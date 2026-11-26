package it.unisa.diversifybe.Benchmark.JMH;

import it.unisa.diversifybe.Model.Commento;
import it.unisa.diversifybe.Model.Forum;
import it.unisa.diversifybe.Model.Post;
import it.unisa.diversifybe.Model.Subcommento;
import it.unisa.diversifybe.Repository.CommentoRepository;
import it.unisa.diversifybe.Repository.ForumRepository;
import it.unisa.diversifybe.Service.CommentoService;

import org.mockito.Mockito;
import org.openjdk.jmh.annotations.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Misura aggiungiCommentoAPost() di CommentoService invocando il metodo originale.
// Operazione misurata: validazione, UUID/Date, scansione di tutti i forum e post, inserimento (post bersaglio: ultimo post dell'ultimo forum).
// Dataset deterministici: SMALL 10x10, MEDIUM 50x20, LARGE 100x50 (forum x post per forum). Il commento aggiunto viene rimosso a ogni invocazione (O(1)).
// ForumRepository e CommentoRepository simulati con Mockito (stubOnly), save() senza effetto; MongoDB escluso, overhead del mock incluso.

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@Threads(1)
public class AggiungiCommentoAPostTestJMH {

    private static final int COMMENTI_PER_POST = 5;

    @Param({"SMALL", "MEDIUM", "LARGE"})
    public String datasetSize;

    private CommentoService service;
    private String idPost;
    private Commento commento;
    private List<Commento> commentiBersaglio;
    private int dimensioneIniziale;

    @Setup(Level.Trial)
    public void setup() {

        int numeroForum;
        int postPerForum;

        switch (datasetSize) {
            case "SMALL" -> {
                numeroForum = 10;
                postPerForum = 10;
            }
            case "MEDIUM" -> {
                numeroForum = 50;
                postPerForum = 20;
            }
            case "LARGE" -> {
                numeroForum = 100;
                postPerForum = 50;
            }
            default -> throw new IllegalArgumentException(
                    "Dataset non valido: " + datasetSize
            );
        }

        List<Forum> forums = creaForum(
                numeroForum, postPerForum, COMMENTI_PER_POST
        );

        // Caso peggiore: il post cercato è l'ultimo dell'ultimo forum.
        Post postBersaglio = forums.get(numeroForum - 1).getPost().get(postPerForum - 1);
        idPost = postBersaglio.getIdPost();
        commentiBersaglio = postBersaglio.getCommenti();
        dimensioneIniziale = commentiBersaglio.size();

        ForumRepository forumRepository = Mockito.mock(
                ForumRepository.class,
                Mockito.withSettings().stubOnly()
        );
        CommentoRepository commentoRepository = Mockito.mock(
                CommentoRepository.class,
                Mockito.withSettings().stubOnly()
        );
        Mockito.when(forumRepository.findAll()).thenReturn(forums);

        // CommentoService usa @Autowired su campi privati (nessun costruttore):
        // le dipendenze vengono iniettate via reflection, senza avviare Spring.
        service = new CommentoService();
        ReflectionTestUtils.setField(service, "forumRepository", forumRepository);
        ReflectionTestUtils.setField(service, "commentoRepository", commentoRepository);

        commento = new Commento();
        commento.setIdAutore("utente-benchmark");
        commento.setContenuto("Commento di prova per il benchmark JMH.");

        // Verifica preliminare: il commento deve finire solo nel post bersaglio.
        resetCommento();
        Commento risultato = service.aggiungiCommentoAPost(idPost, commento);
        if (risultato == null
                || risultato.getIdCommento() == null
                || commentiBersaglio.size() != dimensioneIniziale + 1
                || commentiBersaglio.get(commentiBersaglio.size() - 1) != commento) {
            throw new IllegalStateException("Verifica preliminare del benchmark fallita.");
        }
        commentiBersaglio.remove(commentiBersaglio.size() - 1);
    }

    @Benchmark
    public Commento aggiungiCommentoAPost() {
        // Ogni richiesta reale arriva con un commento senza id/data/subcommenti:
        // si ripristina lo stato iniziale perché il metodo generi UUID e Date ogni volta.
        resetCommento();
        Commento risultato = service.aggiungiCommentoAPost(idPost, commento);
        // Rimozione O(1) dell'elemento aggiunto: la lista non cresce tra le invocazioni.
        commentiBersaglio.remove(commentiBersaglio.size() - 1);
        return risultato;
    }

    @TearDown(Level.Trial)
    public void verificaStato() {
        if (commentiBersaglio.size() != dimensioneIniziale) {
            throw new IllegalStateException("Stato del dataset alterato durante il benchmark.");
        }
    }

    private void resetCommento() {
        commento.setIdCommento(null);
        commento.setDataCreazione(null);
        commento.setSubcommenti(null);
    }

    // Generatore di dati sintetici deterministici (eseguito solo in @Setup).
    // Data fissa: dataset identico tra run baseline e run dopo le modifiche.
    private static final Date DATA_FISSA = new Date(1_700_000_000_000L);

    private static List<Forum> creaForum(int numeroForum, int postPerForum, int commentiPerPost) {

        List<Forum> forums = new ArrayList<>(numeroForum);

        for (int f = 0; f < numeroForum; f++) {

            Forum forum = new Forum();
            forum.setIdForum("forum-" + f);
            forum.setTitolo("Forum " + f);
            forum.setPaese("paese-" + (f % 50));

            List<Post> posts = new ArrayList<>(postPerForum);

            for (int p = 0; p < postPerForum; p++) {

                Post post = new Post();
                post.setIdPost("post-" + f + "-" + p);
                post.setIdForum(forum.getIdForum());
                post.setIdAutore("utente-" + (p % 25));
                post.setTitolo("Post " + p);
                post.setContenuto("Contenuto del post " + p);
                post.setDataCreazione(DATA_FISSA);

                List<Commento> commenti = new ArrayList<>(commentiPerPost + 1);

                for (int c = 0; c < commentiPerPost; c++) {
                    Commento commento = new Commento();
                    commento.setIdCommento("commento-" + f + "-" + p + "-" + c);
                    commento.setIdPost(post.getIdPost());
                    commento.setIdAutore("utente-" + (c % 25));
                    commento.setContenuto("Commento " + c);
                    commento.setDataCreazione(DATA_FISSA);
                    commento.setSubcommenti(new ArrayList<Subcommento>(1));
                    commenti.add(commento);
                }

                post.setCommenti(commenti);
                posts.add(post);
            }

            forum.setPost(posts);
            forums.add(forum);
        }

        return forums;
    }
}
