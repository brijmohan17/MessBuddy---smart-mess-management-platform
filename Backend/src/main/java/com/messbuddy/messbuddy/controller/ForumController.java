package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.entity.ForumPost;
import com.messbuddy.messbuddy.entity.User;
import com.messbuddy.messbuddy.repository.AuthRepository;
import com.messbuddy.messbuddy.repository.ForumPostRepository;
import com.messbuddy.messbuddy.repository.MessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

	private final ForumPostRepository forumPostRepository;
	private final AuthRepository authRepository;
	private final MessRepository messRepository;

	@PostMapping("/user/{userId}")
	public ForumPost createPost(@PathVariable String userId, @RequestBody Map<String, Object> body) {
		User author = authRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to create a post"));

		String title = (String) body.get("title");
		String content = (String) body.get("content");
		String type = (String) body.get("type");
		String messId = (String) body.get("messId");

		if (title == null || title.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");

		ForumPost post = ForumPost.builder()
				.title(title)
				.content(content)
				.author(userId)
				.type(type == null ? "general" : type)
				.messId(messId)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();

		// handle poll options if provided
		Object pollOptionsObj = body.get("pollOptions");
		if ("poll".equalsIgnoreCase(post.getType()) && pollOptionsObj instanceof List<?> list) {
			List<ForumPost.PollOption> opts = list.stream()
					.filter(Objects::nonNull)
					.map(Object::toString)
					.map(s -> new ForumPost.PollOption(s, new ArrayList<>()))
					.collect(Collectors.toList());
			post.setPollOptions(opts);
			post.setIsPollActive(true);
		}

		return forumPostRepository.save(post);
	}

	// Compatibility endpoints to match frontend routes (/forum/posts/...)
	@PostMapping("/posts/create/{userId}")
	public ForumPost createPostCompat(@PathVariable String userId, @RequestBody Map<String, Object> body) {
		return createPost(userId, body);
	}

	@GetMapping
	public Map<String, Object> getPosts(@RequestParam(required = false) String messId,
										@RequestParam(required = false) String type,
										@RequestParam(required = false) String search,
										@RequestParam(defaultValue = "1") int page,
										@RequestParam(defaultValue = "10") int limit) {
		List<ForumPost> all = forumPostRepository.findAll();
		String normalizedMessId = (messId == null || messId.isBlank()) ? null : messId;
		String normalizedType = (type == null || type.isBlank()) ? null : type;
		String normalizedSearch = (search == null || search.isBlank()) ? null : search;

		List<ForumPost> filtered = all.stream()
				.filter(p -> normalizedMessId == null || (p.getMessId() != null && p.getMessId().equals(normalizedMessId)))
				.filter(p -> normalizedType == null || (p.getType() != null && p.getType().equalsIgnoreCase(normalizedType)))
				.filter(p -> {
					if (normalizedSearch == null) return true;
					String s = normalizedSearch.toLowerCase();
					return (p.getTitle() != null && p.getTitle().toLowerCase().contains(s)) ||
							(p.getContent() != null && p.getContent().toLowerCase().contains(s));
				})
				.sorted(Comparator.comparing(ForumPost::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.collect(Collectors.toList());

		int totalPosts = filtered.size();
		int totalPages = (int) Math.ceil(totalPosts / (double) limit);
		int skip = (page - 1) * limit;

		List<ForumPost> pageItems = filtered.stream().skip(skip).limit(limit).collect(Collectors.toList());

		// Map ForumPost to response objects with populated author and comment user info
		List<Map<String, Object>> postsResp = new ArrayList<>();
		for (ForumPost p : pageItems) {
			Map<String, Object> postMap = new LinkedHashMap<>();
			postMap.put("_id", p.getId());
			postMap.put("title", p.getTitle());
			postMap.put("content", p.getContent());
			postMap.put("type", p.getType());
			postMap.put("isPollActive", p.getIsPollActive());
			postMap.put("pollOptions", p.getPollOptions());
			// populate author
			Map<String, Object> authorMap = new LinkedHashMap<>();
			if (p.getAuthor() != null) {
				authRepository.findById(p.getAuthor()).ifPresent(u -> {
					authorMap.put("_id", u.getId());
					authorMap.put("username", u.getUsername());
				});
			}
			postMap.put("author", authorMap);

			// populate mess
			Map<String, Object> messMap = new LinkedHashMap<>();
			if (p.getMessId() != null) {
				messRepository.findById(p.getMessId()).ifPresent(m -> {
					messMap.put("_id", m.getId());
					messMap.put("Mess_Name", m.getMess_Name());
				});
			}
			postMap.put("messId", messMap.isEmpty() ? null : messMap);

			// comments with populated user
			List<Map<String, Object>> commentsList = new ArrayList<>();
			for (ForumPost.Comment c : p.getComments()) {
				Map<String, Object> cMap = new LinkedHashMap<>();
				cMap.put("_id", c.getId());
				cMap.put("content", c.getContent());
				cMap.put("createdAt", c.getCreatedAt());
				Map<String, Object> cUser = new LinkedHashMap<>();
				if (c.getUserId() != null) {
					authRepository.findById(c.getUserId()).ifPresent(u -> {
						cUser.put("_id", u.getId());
						cUser.put("username", u.getUsername());
					});
				}
				cMap.put("userId", cUser.isEmpty() ? Map.of("_id", c.getUserId()) : cUser);
				cMap.put("likes", c.getLikes());
				commentsList.add(cMap);
			}
			postMap.put("comments", commentsList);

			postMap.put("likes", p.getLikes());
			postMap.put("createdAt", p.getCreatedAt());

			postsResp.add(postMap);
		}

		Map<String, Object> resp = new LinkedHashMap<>();
		resp.put("posts", postsResp);
		Map<String, Object> pagination = new LinkedHashMap<>();
		pagination.put("currentPage", page);
		pagination.put("totalPages", totalPages);
		pagination.put("totalPosts", totalPosts);
		pagination.put("hasNextPage", page < totalPages);
		pagination.put("hasPrevPage", page > 1);
		resp.put("pagination", pagination);
		return resp;
	}

	@GetMapping("/posts")
	public Map<String, Object> getPostsCompat(@RequestParam(required = false) String messId,
											  @RequestParam(required = false) String type,
											  @RequestParam(required = false) String search,
											  @RequestParam(defaultValue = "1") int page,
											  @RequestParam(defaultValue = "10") int limit) {
		return getPosts(messId, type, search, page, limit);
	}

	@PostMapping("/{postId}/comment/user/{userId}")
	public ForumPost addComment(@PathVariable String postId, @PathVariable String userId, @RequestBody Map<String, Object> body) {
		authRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to comment"));

		ForumPost post = forumPostRepository.findById(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

		String content = (String) body.get("content");
		if (content == null || content.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content required");

		ForumPost.Comment comment = new ForumPost.Comment(UUID.randomUUID().toString(), userId, content, new ArrayList<>(), LocalDateTime.now());
		post.getComments().add(comment);
		ForumPost saved = forumPostRepository.save(post);
		return saved;
	}

	@PostMapping("/posts/{postId}/comment/{userId}")
	public ForumPost addCommentCompat(@PathVariable String postId, @PathVariable String userId, @RequestBody Map<String, Object> body) {
		return addComment(postId, userId, body);
	}

	@PostMapping("/{postId}/vote/user/{userId}")
	public ForumPost votePoll(@PathVariable String postId, @PathVariable String userId, @RequestBody Map<String, Object> body) {
		authRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to vote"));

		ForumPost post = forumPostRepository.findById(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

		if (post.getPollOptions() == null || !Boolean.TRUE.equals(post.getIsPollActive())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Poll not found or inactive");
		}

		Integer optionIndex = null;
		try {
			optionIndex = Integer.parseInt(String.valueOf(body.get("optionIndex")));
		} catch (Exception ignored) {}

		if (optionIndex == null || optionIndex < 0 || optionIndex >= post.getPollOptions().size()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid option index");
		}

		// remove previous votes
		post.getPollOptions().forEach(opt -> opt.getVotes().removeIf(v -> v.equals(userId)));
		post.getPollOptions().get(optionIndex).getVotes().add(userId);
		post.setUpdatedAt(LocalDateTime.now());
		return forumPostRepository.save(post);
	}

	@PostMapping("/posts/{postId}/vote/{userId}")
	public ForumPost votePollCompat(@PathVariable String postId, @PathVariable String userId, @RequestBody Map<String, Object> body) {
		return votePoll(postId, userId, body);
	}

	@PostMapping("/{postId}/like/user/{userId}")
	public ForumPost likePost(@PathVariable String postId, @PathVariable String userId) {
		authRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to like posts"));

		ForumPost post = forumPostRepository.findById(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

		if (post.getLikes().contains(userId)) {
			post.getLikes().remove(userId);
		} else {
			post.getLikes().add(userId);
		}
		post.setUpdatedAt(LocalDateTime.now());
		return forumPostRepository.save(post);
	}

	@PostMapping("/posts/{postId}/like/{userId}")
	public ForumPost likePostCompat(@PathVariable String postId, @PathVariable String userId) {
		return likePost(postId, userId);
	}

	@PutMapping("/{postId}/user/{userId}")
	public ForumPost updatePost(@PathVariable String postId, @PathVariable String userId, @RequestBody Map<String, Object> body) {
		ForumPost post = forumPostRepository.findById(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

		if (!post.getAuthor().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own posts");

		String title = (String) body.get("title");
		String content = (String) body.get("content");
		String type = (String) body.get("type");

		post.setTitle(title);
		post.setContent(content);
		post.setType(type);

		Object pollOptionsObj = body.get("pollOptions");
		if ("poll".equalsIgnoreCase(type) && pollOptionsObj instanceof List<?> list) {
			List<ForumPost.PollOption> newOpts = new ArrayList<>();
			for (Object o : list) {
				String text = String.valueOf(o);
				Optional<ForumPost.PollOption> existing = post.getPollOptions().stream().filter(p -> p.getText().equals(text)).findFirst();
				newOpts.add(existing.orElse(new ForumPost.PollOption(text, new ArrayList<>())));
			}
			post.setPollOptions(newOpts);
		}

		post.setUpdatedAt(LocalDateTime.now());
		return forumPostRepository.save(post);
	}

	@PutMapping("/posts/{postId}/{userId}")
	public ForumPost updatePostCompat(@PathVariable String postId, @PathVariable String userId, @RequestBody Map<String, Object> body) {
		return updatePost(postId, userId, body);
	}

	@DeleteMapping("/{postId}/user/{userId}")
	public Map<String, String> deletePost(@PathVariable String postId, @PathVariable String userId) {
		ForumPost post = forumPostRepository.findById(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

		if (!post.getAuthor().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own posts");

		forumPostRepository.deleteById(postId);
		return Map.of("message", "Post deleted successfully");
	}

	@DeleteMapping("/posts/{postId}/{userId}")
	public Map<String, String> deletePostCompat(@PathVariable String postId, @PathVariable String userId) {
		return deletePost(postId, userId);
	}

	@PostMapping("/{postId}/comment/{commentId}/like/user/{userId}")
	public ForumPost likeComment(@PathVariable String postId, @PathVariable String commentId, @PathVariable String userId) {
		authRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to like comments"));

		ForumPost post = forumPostRepository.findById(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

		ForumPost.Comment comment = post.getComments().stream().filter(c -> c.getId().equals(commentId)).findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

		if (comment.getLikes().contains(userId)) {
			comment.getLikes().remove(userId);
		} else {
			comment.getLikes().add(userId);
		}
		post.setUpdatedAt(LocalDateTime.now());
		return forumPostRepository.save(post);
	}

	@PostMapping("/posts/{postId}/comments/{commentId}/like/{userId}")
	public ForumPost likeCommentCompat(@PathVariable String postId, @PathVariable String commentId, @PathVariable String userId) {
		return likeComment(postId, commentId, userId);
	}

	@DeleteMapping("/{postId}/comment/{commentId}/user/{userId}")
	public ForumPost deleteComment(@PathVariable String postId, @PathVariable String commentId, @PathVariable String userId) {
		ForumPost post = forumPostRepository.findById(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

		int idx = -1;
		for (int i = 0; i < post.getComments().size(); i++) {
			if (post.getComments().get(i).getId().equals(commentId)) {
				idx = i;
				break;
			}
		}
		if (idx == -1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");

		if (!post.getComments().get(idx).getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments");

		post.getComments().remove(idx);
		post.setUpdatedAt(LocalDateTime.now());
		return forumPostRepository.save(post);
	}

	@DeleteMapping("/posts/{postId}/comments/{commentId}/{userId}")
	public ForumPost deleteCommentCompat(@PathVariable String postId, @PathVariable String commentId, @PathVariable String userId) {
		return deleteComment(postId, commentId, userId);
	}
}
