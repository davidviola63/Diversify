package it.unisa.diversifybe.Controller;

import it.unisa.diversifybe.Model.Commento;
import it.unisa.diversifybe.Model.Forum;
import it.unisa.diversifybe.Model.Post;
import it.unisa.diversifybe.Model.Subcommento;
import it.unisa.diversifybe.Repository.ForumRepository;
import it.unisa.diversifybe.Service.CommentoService;
import it.unisa.diversifybe.Service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/like")
@CrossOrigin
public class LikeController {

    private final CommentoService commentoService;
    private final PostService postService;
    private final ForumRepository forumRepository;

    @Autowired
    public LikeController(final CommentoService commentoService,
                          final PostService postService,
                          final ForumRepository forumRepository) {
        this.commentoService = commentoService;
        this.postService = postService;
        this.forumRepository = forumRepository;
    }

    /**
     * Aggiunge un like a un post specifico.
     *
     * @param postId L'ID del post a cui aggiungere il like.
     * @return Una risposta HTTP con lo stato dell'operazione.
     */
    @PostMapping("/post/{postId}")
    public ResponseEntity<?> addLikeToPost(@PathVariable final String postId) {
        final List<Forum> forums = forumRepository.findAll();

        for (final Forum forum : forums) {
            final List<Post> posts = forum.getPost();
            if (posts != null) {
                for (final Post post : posts) {
                    if (post.getIdPost().equals(postId)) {
                        post.setLike(post.getLike() + 1);
                        forum.setPost(posts);
                        forumRepository.save(forum);
                    }
                }
            }
        }

        return ResponseEntity.ok("Like aggiunto al post con successo");
    }

    /**
     * Rimuove un like da un post specifico.
     *
     * @param postId L'ID del post da cui rimuovere il like.
     * @return Una risposta HTTP con lo stato dell'operazione.
     */
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<?> removeLikeFromPost(@PathVariable final String postId) {
        final Optional<Post> postOptional = postService.findById(postId);

        if (postOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Post non trovato");
        }

        final Post post = postOptional.get();
        if (post.getLike() > 0) {
            post.setLike(post.getLike() - 1);
            postService.save(post);
        }

        return ResponseEntity.ok("Like rimosso dal post con successo");
    }

    /**
     * Aggiunge un like a un commento specifico.
     *
     * @param commentoId L'ID del commento a cui aggiungere il like.
     * @return Una risposta HTTP con lo stato dell'operazione.
     */
    @PostMapping("/commento/{commentoId}")
    public ResponseEntity<?> addLikeToCommento(@PathVariable final String commentoId) {
        final List<Forum> forums = forumRepository.findAll();

        for (final Forum forum : forums) {
            final List<Post> posts = forum.getPost();

            if (posts == null) {
                continue;
            }

            for (final Post post : posts) {
                final List<Commento> commenti = post.getCommenti();

                for (final Commento commento : commenti) {
                    if (commento.getIdCommento() != null
                            && commento.getIdCommento().equals(commentoId)) {
                        commento.setLike(commento.getLike() + 1);
                        post.setCommenti(commenti);
                        forum.setPost(posts);
                        forumRepository.save(forum);
                    }
                }
            }
        }

        return ResponseEntity.ok("Like aggiunto al commento con successo");
    }

    /**
     * Rimuove un like da un commento specifico.
     *
     * @param commentoId L'ID del commento da cui rimuovere il like.
     * @return Una risposta HTTP con lo stato dell'operazione.
     */
    @DeleteMapping("/commento/{commentoId}")
    public ResponseEntity<?> removeLikeFromCommento(@PathVariable final String commentoId) {
        final Optional<Commento> commentoOptional = commentoService.findById(commentoId);

        if (commentoOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Commento non trovato");
        }

        final Commento commento = commentoOptional.get();
        if (commento.getLike() > 0) {
            commento.setLike(commento.getLike() - 1);
            commentoService.save(commento);
        }

        return ResponseEntity.ok("Like rimosso dal commento con successo");
    }
}