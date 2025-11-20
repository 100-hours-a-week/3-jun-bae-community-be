package com.ktb.community.repository;

import com.ktb.community.repository.projection.PostSummaryProjection;
import com.ktb.community.support.CursorPage;
import com.ktb.community.support.PostSortType;

public interface PostQueryRepository {

    CursorPage<PostSummaryProjection> findAllByCursor(Long cursorId, int size, PostSortType sortType);
}
