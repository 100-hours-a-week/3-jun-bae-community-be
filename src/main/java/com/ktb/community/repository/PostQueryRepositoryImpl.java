package com.ktb.community.repository;

import com.ktb.community.entity.QPost;
import com.ktb.community.entity.QPostStats;
import com.ktb.community.entity.QUser;
import com.ktb.community.repository.projection.PostSummaryProjection;
import com.ktb.community.support.CursorPage;
import com.ktb.community.support.PostSortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

@Repository
@RequiredArgsConstructor
public class PostQueryRepositoryImpl implements PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPage<PostSummaryProjection> findAllByCursor(Long cursorId, int size, PostSortType sortType) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        QPostStats postStats = QPostStats.postStats;
        NumberExpression<Long> likeCountExpr = postStats.likeCount.coalesce(0L);
        NumberExpression<Long> replyCountExpr = postStats.replyCount.coalesce(0L);
        NumberExpression<Long> viewCountExpr = postStats.viewCount.coalesce(0L);

        List<PostSummaryProjection> results = queryFactory
                .select(Projections.constructor(
                        PostSummaryProjection.class,
                        post.id,
                        post.title,
                        post.content,
                        user.id,
                        user.nickname,
                        post.customAuthorName,
                        post.createdAt,
                        post.updatedAt,
                        postStats.viewCount.coalesce(0L),
                        postStats.likeCount.coalesce(0L),
                        postStats.replyCount.coalesce(0L)
                ))
                .from(post)
                .join(post.user, user)
                .leftJoin(postStats).on(postStats.post.eq(post))
                .where(
                        post.deletedAt.isNull(),
                        cursorPredicate(cursorId, sortType, post, likeCountExpr, replyCountExpr, viewCountExpr)
                )
                .orderBy(orderSpecifiers(sortType, post, likeCountExpr, replyCountExpr, viewCountExpr))
                .limit(size + 1L)
                .fetch();

        boolean hasNext = results.size() > size;
        Long nextCursor = null;
        if (hasNext) {
            PostSummaryProjection last = results.remove(size);
            nextCursor = last.id();
        } else if (!results.isEmpty()) {
            nextCursor = results.get(results.size() - 1).id();
        }

        return new CursorPage<>(results, nextCursor, hasNext);
    }

    private BooleanExpression cursorPredicate(Long cursorId,
                                              PostSortType sortType,
                                              QPost post,
                                              NumberExpression<Long> likeCountExpr,
                                              NumberExpression<Long> replyCountExpr,
                                              NumberExpression<Long> viewCountExpr) {
        if (cursorId == null) {
            return null;
        }
        return switch (sortType) {
            case LATEST -> post.id.lt(cursorId);
            case LIKES -> rankingPredicate(cursorId, post, likeCountExpr, SortCursorInfo::likeCount);
            case COMMENTS -> rankingPredicate(cursorId, post, replyCountExpr, SortCursorInfo::replyCount);
            case VIEWS ->  rankingPredicate(cursorId, post, viewCountExpr, SortCursorInfo::viewCount);
        };
    }

    private BooleanExpression rankingPredicate(Long cursorId,
                                               QPost post,
                                               NumberExpression<Long> sortExpr,
                                               Function<SortCursorInfo, Long> cursorValueExtractor) {
        SortCursorInfo cursorInfo = loadCursorInfo(cursorId);
        if (cursorInfo == null) {
            return post.id.lt(cursorId);
        }
        Long value = cursorValueExtractor.apply(cursorInfo);
        long cursorValue = value == null ? 0L : value;
        return sortExpr.lt(cursorValue)
                .or(sortExpr.eq(cursorValue).and(post.id.lt(cursorInfo.postId())));
    }

    private OrderSpecifier<?>[] orderSpecifiers(PostSortType sortType,
                                                QPost post,
                                                NumberExpression<Long> likeCountExpr,
                                                NumberExpression<Long> replyCountExpr,
                                                NumberExpression<Long> viewCountExpr) {
        return switch (sortType) {
            case LATEST -> new OrderSpecifier[]{post.id.desc()};
            case LIKES -> new OrderSpecifier[]{likeCountExpr.desc(), post.id.desc()};
            case COMMENTS -> new OrderSpecifier[]{replyCountExpr.desc(), post.id.desc()};
            case VIEWS ->  new OrderSpecifier[]{viewCountExpr.desc() ,post.id.desc()};
        };
    }

    private SortCursorInfo loadCursorInfo(Long cursorId) {
        if (cursorId == null) {
            return null;
        }
        QPost cursorPost = new QPost("cursorPost");
        QPostStats cursorStats = new QPostStats("cursorPostStats");

        return queryFactory
                .select(Projections.constructor(
                        SortCursorInfo.class,
                        cursorPost.id,
                        cursorStats.likeCount.coalesce(0L),
                        cursorStats.replyCount.coalesce(0L),
                        cursorStats.viewCount.coalesce(0L)
                ))
                .from(cursorPost)
                .leftJoin(cursorStats).on(cursorStats.post.eq(cursorPost))
                .where(cursorPost.id.eq(cursorId))
                .fetchOne();
    }

    private record SortCursorInfo(Long postId, Long likeCount, Long replyCount, Long viewCount) {
    }
}
