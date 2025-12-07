package com.ktb.community.service;

import com.ktb.community.dto.post.PostSummaryResponse;
import com.ktb.community.entity.File;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.PostLike;
import com.ktb.community.entity.PostStats;
import com.ktb.community.entity.User;
import com.ktb.community.repository.FileRepository;
import com.ktb.community.repository.PostLikeRepository;
import com.ktb.community.repository.PostRepository;
import com.ktb.community.repository.projection.PostSummaryProjection;
import com.ktb.community.support.CursorPage;
import com.ktb.community.support.PostSortType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private OwnershipVerifier ownershipVerifier;
    @Mock
    private PostStatsService postStatsService;
    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostService postService;

    private User author;

    @BeforeEach
    void setUp() {
        author = createUser(1L);
    }

    @Test
    void createPost_attachesFilesAndInitializesStats() {
        List<Long> fileIds = List.of(10L, 11L);
        File file1 = File.pending("a.txt", "key1", "url1", 1000);
        File file2 = File.pending("b.txt", "key2", "url2", 9000);
        when(fileRepository.findByIdIn(fileIds)).thenReturn(List.of(file1, file2));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 100L);
            return post;
        });
        PostStats stats = PostStats.initialize(Post.create(author, "init", "init"));
        when(postStatsService.initialize(any(Post.class))).thenReturn(stats);

        Post result = postService.createPost(author, "title", "content", fileIds);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getFiles()).hasSize(2);
        assertThat(file1.isCommitted()).isTrue();
        assertThat(file2.isCommitted()).isTrue();
        verify(postStatsService).initialize(result);
    }

    @Test
    void createPost_whenFileMissing_throwsNotFound() {
        List<Long> fileIds = List.of(10L, 11L);
        when(fileRepository.findByIdIn(fileIds)).thenReturn(List.of(File.pending("a", "k", "u", 1)));

        assertThatThrownBy(() -> postService.createPost(author, "t", "c", fileIds))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
        verify(postRepository, never()).save(any());
    }

    @Test
    void getPosts_returnsMappedCursorPage() {
        PostSummaryProjection projection = new PostSummaryProjection(
                1L, "title", "content", 2L, "nick", null,
                Instant.now(), Instant.now(), 3L, 4L, 5L
        );
        CursorPage<PostSummaryProjection> source = new CursorPage<>(List.of(projection), 9L, true);
        when(postRepository.findAllByCursor(5L, 20, PostSortType.LATEST)).thenReturn(source);

        CursorPage<PostSummaryResponse> page = postService.getPosts(5L, 20, PostSortType.LATEST);

        assertThat(page.getContents()).hasSize(1);
        assertThat(page.getNextCursor()).isEqualTo(9L);
        assertThat(page.isHasNext()).isTrue();
        PostSummaryResponse response = page.getContents().getFirst();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.authorId()).isEqualTo(2L);
    }

    @Test
    void getPostOrThrow_whenFound_returnsPost() {
        Post post = createPost(10L, author);
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(post));

        Post result = postService.getPostOrThrow(10L);

        assertThat(result).isSameAs(post);
    }

    @Test
    void getPostOrThrow_whenMissing_throwsNotFound() {
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostOrThrow(10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void viewPost_loadsPostAndIncrementsViewCount() {
        Post post = createPost(1L, author);
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(postStatsService.increaseView(1L)).thenReturn(PostStats.initialize(post));

        Post result = postService.viewPost(1L);

        assertThat(result).isSameAs(post);
        verify(postStatsService).increaseView(1L);
    }

    @Test
    void updatePost_replacesAttachmentsWhenIdsProvided() {
        Post post = createPost(1L, author);
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        List<File> files = List.of(
                File.pending("a", "k1", "u1", 1),
                File.pending("b", "k2", "u2", 2)
        );
        when(fileRepository.findByIdIn(List.of(5L, 6L))).thenReturn(files);

        Post updated = postService.updatePost(1L, author, "new", "body", List.of(5L, 6L));

        assertThat(updated.getTitle()).isEqualTo("new");
        assertThat(updated.getContent()).isEqualTo("body");
        assertThat(updated.getFiles()).hasSize(2);
        verify(ownershipVerifier).check(post, author, "Only author can modify this post");
        assertThat(files).allMatch(File::isCommitted);
    }

    @Test
    void updatePost_whenFileIdsNull_skipsAttachmentLoading() {
        Post post = createPost(1L, author);
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));

        Post result = postService.updatePost(1L, author, "new", "body", null);

        assertThat(result.getTitle()).isEqualTo("new");
        assertThat(result.getFiles()).isEmpty();
        verifyNoInteractions(fileRepository);
    }

    @Test
    void deletePost_softDeletesAfterOwnershipCheck() {
        Post post = createPost(1L, author);
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L, author);

        verify(ownershipVerifier).check(post, author, "Only author can modify this post");
        assertThat(post.isDeleted()).isTrue();
    }

    @Test
    void likePost_whenNotPreviouslyLiked_createsLikeAndIncrementsStats() {
        Post post = createPost(1L, author);
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByPostIdAndUserId(1L, author.getId())).thenReturn(false);
        PostStats stats = PostStats.initialize(post);
        ReflectionTestUtils.setField(stats, "likeCount", 2L);
        when(postStatsService.increaseLike(1L)).thenReturn(stats);
        when(postLikeRepository.save(any(PostLike.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostService.PostLikeResult result = postService.likePost(1L, author);

        assertThat(result.liked()).isTrue();
        assertThat(result.likeCount()).isEqualTo(2L);
        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isSameAs(post);
    }

    @Test
    void likePost_whenAlreadyLiked_returnsCurrentCount() {
        Post post = createPost(1L, author);
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByPostIdAndUserId(1L, author.getId())).thenReturn(true);
        PostStats stats = PostStats.initialize(post);
        ReflectionTestUtils.setField(stats, "likeCount", 5L);
        when(postStatsService.getStats(1L)).thenReturn(stats);

        PostService.PostLikeResult result = postService.likePost(1L, author);

        assertThat(result.liked()).isTrue();
        assertThat(result.likeCount()).isEqualTo(5L);
        verify(postLikeRepository, never()).save(any());
        verify(postStatsService, never()).increaseLike(anyLong());
    }

    @Test
    void unlikePost_whenExistingLike_removesAndDecrements() {
        Post post = createPost(1L, author);
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        PostLike existing = PostLike.of(author, post);
        when(postLikeRepository.findByPostIdAndUserId(1L, author.getId())).thenReturn(Optional.of(existing));
        PostStats stats = PostStats.initialize(post);
        ReflectionTestUtils.setField(stats, "likeCount", 4L);
        when(postStatsService.decreaseLike(1L)).thenReturn(stats);
        doNothing().when(postLikeRepository).delete(existing);

        PostService.PostLikeResult result = postService.unlikePost(1L, author);

        assertThat(result.liked()).isFalse();
        assertThat(result.likeCount()).isEqualTo(4L);
        verify(postLikeRepository).delete(existing);
    }
    @Test
    void unlikePost_whenNoExistingLike_returnsCurrentStats() {
        Post post = createPost(1L, author);
        when(postRepository.findWithFilesByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostIdAndUserId(1L, author.getId())).thenReturn(Optional.empty());
        PostStats stats = PostStats.initialize(post);
        ReflectionTestUtils.setField(stats, "likeCount", 7L);
        when(postStatsService.getStats(1L)).thenReturn(stats);

        PostService.PostLikeResult result = postService.unlikePost(1L, author);

        assertThat(result.liked()).isFalse();
        assertThat(result.likeCount()).isEqualTo(7L);
        verify(postLikeRepository, never()).delete(any(PostLike.class));
    }

    private static User createUser(Long id) {
        User user = User.builder()
                .email("user@example.com")
                .password("encoded")
                .nickname("user")
                .active(true)
                .admin(false)
                .deleted(false)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Post createPost(Long id, User user) {
        Post post = Post.create(user, "title", "content");
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
