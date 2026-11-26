package it.unisa.diversifybe.Service;

import it.unisa.diversifybe.Model.Commento;
import it.unisa.diversifybe.Model.Forum;
import it.unisa.diversifybe.Model.Post;
import it.unisa.diversifybe.Model.Subcommento;
import it.unisa.diversifybe.Repository.CommentoRepository;
import it.unisa.diversifybe.Repository.ForumRepository;
import it.unisa.diversifybe.Repository.PostRepository;
import it.unisa.diversifybe.Repository.SubcommentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommentoService {

    private final CommentoRepository commentoRepository;
    private final ForumRepository forumRepository;

    @Autowired
    public CommentoService(final CommentoRepository commentoRepository,
                           final ForumRepository forumRepository) {
        this.commentoRepository = commentoRepository;
        this.forumRepository = forumRepository;
    }

    /**
     * Aggiunge un commento a un post specifico.
     *
     * @param idPost   L'ID del post a cui aggiungere il commento.
     * @param commento Il commento da aggiungere.
     * @return Il commento salvato.
     * @throws IllegalArgumentException se l'ID del post o il commento non sono validi.
     */
    public Commento aggiungiCommentoAPost(final String idPost, final Commento commento) {
        validaCommento(idPost, commento);

        if (commento.getIdCommento() == null || commento.getIdCommento().isEmpty()) {
            commento.setIdCommento(UUID.randomUUID().toString());
        }

        // Imposta i valori mancanti
        if (commento.getDataCreazione() == null) {
            commento.setDataCreazione(new Date());
        }

        if (commento.getSubcommenti() == null) {
            commento.setSubcommenti(new ArrayList<>());
        }

        commento.setLike(0);

        final List<Forum> forums = forumRepository.findAll();

        for (final Forum forum : forums) {
            final List<Post> posts = forum.getPost();

            if (posts == null) {
                continue;
            }

            for (final Post post : posts) {
                if (post.getIdPost().equals(idPost)) {
                    final List<Commento> commenti = post.getCommenti();
                    commenti.add(commento);
                    post.setCommenti(commenti);
                    forum.setPost(posts);
                    forumRepository.save(forum);
                }
            }
        }

        return commento;
    }

    private void validaCommento(final String idPost, final Commento commento) {
        if (idPost == null || idPost.isBlank()) {
            throw new IllegalArgumentException("L'ID del post non può essere nullo o vuoto.");
        }

        if (commento == null) {
            throw new IllegalArgumentException("Il commento non può essere nullo.");
        }

        if (commento.getIdAutore() == null || commento.getIdAutore().isBlank() ||
                commento.getContenuto() == null || commento.getContenuto().isBlank()) {
            throw new IllegalArgumentException("Il commento deve avere un autore e un contenuto valido.");
        }
    }

    /**
     * Aggiunge un subcommento a un commento principale.
     *
     * @param idCommento  L'ID del commento principale.
     * @param subcommento Il subcommento da aggiungere.
     * @return Il subcommento salvato.
     * @throws IllegalArgumentException se il subcommento è nullo o contiene campi mancanti/non validi.
     */
    public Subcommento aggiungiSubcommentoACommento(final String idCommento, final Subcommento subcommento) {
        validaSubcommento(idCommento, subcommento);

        // Imposta i dati del subcommento
        subcommento.setDataCreazione(new Date());

        final List<Forum> forums = forumRepository.findAll();

        for (final Forum forum : forums) {
            final List<Post> posts = forum.getPost();

            if (posts == null) {
                continue;
            }

            for (final Post post : posts) {
                final List<Commento> commenti = post.getCommenti();

                for (final Commento commento : commenti) {
                    if (commento.getIdCommento() != null && commento.getIdCommento().equals(idCommento)) {
                        final List<Subcommento> subcommenti = commento.getSubcommenti();
                        subcommenti.add(subcommento);
                        commento.setSubcommenti(subcommenti);
                        post.setCommenti(commenti);
                        forum.setPost(posts);
                        forumRepository.save(forum);
                    }
                }
            }
        }

        return subcommento;
    }

    private void validaSubcommento(final String idCommento, final Subcommento subcommento) {
        if (idCommento == null || idCommento.isBlank()) {
            throw new IllegalArgumentException("L'ID del commento non può essere nullo o vuoto.");
        }

        if (subcommento == null) {
            throw new IllegalArgumentException("Il subcommento non può essere nullo.");
        }

        if (subcommento.getIdAutore() == null || subcommento.getIdAutore().isBlank() ||
                subcommento.getContenuto() == null || subcommento.getContenuto().isBlank()) {
            throw new IllegalArgumentException("Il subcommento deve avere un autore e un contenuto valido.");
        }
    }

    /**
     * Recupera tutti i commenti associati a un post.
     *
     * @param idPost L'ID del post.
     * @return La lista dei commenti.
     */
    public List<Commento> trovaCommentiPerPost(final String idPost) {
        return commentoRepository.findByIdPost(idPost);
    }

    /**
     * Trova un commento in base al suo ID.
     *
     * @param commentoId L'ID del commento da trovare.
     * @return Un {@link Optional} contenente il commento se trovato,
     * altrimenti un {@link Optional#empty()} se non esiste un commento con l'ID specificato.
     */
    public Optional<Commento> findById(final String commentoId) {
        return commentoRepository.findById(commentoId);
    }

    /**
     * Salva o aggiorna un commento nel database.
     *
     * @param commento L'oggetto {@link Commento} da salvare o aggiornare.
     *                 Se il commento esiste già, verrà aggiornato; altrimenti, verrà creato un nuovo record.
     */
    public void save(final Commento commento) {
        commentoRepository.save(commento);
    }

    public List<Subcommento> trovaSubcommentiPerCommento(final String idCommento) {
        return commentoRepository.findById(idCommento)
                .map(Commento::getSubcommenti)
                .orElse(List.of());
    }
}