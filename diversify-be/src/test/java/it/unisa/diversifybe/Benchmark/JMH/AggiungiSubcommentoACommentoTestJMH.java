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

// Misura aggiungiSubcommentoACommento() di CommentoService invocando il metodo originale.
// Operazione misurata: validazione e scansione su tre livelli (forum, post, commenti) fino al commento bersaglio (ultimo commento dell'ultimo post dell'ultimo forum).
// Dataset deterministici: SMALL 10x10x5, MEDIUM 50x20x10, LARGE 100x50x20 (forum x post x commenti). Il subcommento aggiunto viene rimosso a ogni invocazione (O(1)).
// ForumRepository e CommentoRepository simulati con Mockito (stubOnly), save() senza effetto; MongoDB escluso, overhead del mock incluso.

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@Threads(1)
public class AggiungiSubcommentoACommentoTestJMH {

    @Param({"SMALL", "MEDIUM", "LARGE"})
    public String datasetSize;

    private CommentoService service;
    private String idCommento;
    private Subcommento subcommento;
    private List<Subcommento> subcommentiBersaglio;
    private int dimensioneIniziale;

    @Setup(Level.Trial)
    public void setup() {

        int numeroForum;
        int postPerForum;
        int commentiPerPost;

        switch (datasetSize) {
            case "SMALL" -> {
                numeroForum = 10;
                postPerForum = 10;
                commentiPerPost = 5;
            }
            case "MEDIUM" -> {
                numeroForum = 50;
                postPerForum = 20;
                commentiPerPost = 10;
            }
            case "LARGE" -> {
                numeroForum = 100;
                postPerForum = 50;
                commentiPerPost = 20;
            }
            default -> throw new IllegalArgumentException(
                    "Dataset non valido: " + datasetSize
            );
        }

        List<Forum> forums = creaForum(
                numeroForum, postPerForum, commentiPerPost
        );

        // Caso peggiore: il commento cercato è l'ultimo dell'ultimo post dell'ultimo forum.
        Commento commentoBersaglio = forums.get(numeroForum - 1)
                .getPost().get(postPerForum - 1)
                .getCommenti().get(commentiPerPost - 1);
        idCommento = commentoBersaglio.getIdCommento();
        subcommentiBersaglio = commentoBersaglio.getSubcommenti();
        dimensioneIniziale = subcommentiBersaglio.size();

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

        subcommento = new Subcommento();
        subcommento.setIdAutore("utente-benchmark");
        subcommento.setContenuto("Risposta di prova per il benchmark JMH.");

        // Verifica preliminare: il subcommento deve finire solo nel commento bersaglio.
        Subcommento risultato = service.aggiungiSubcommentoACommento(idCommento, subcommento);
        if (risultato == null
                || risultato.getDataCreazione() == null
                || subcommentiBersaglio.size() != dimensioneIniziale + 1
                || subcommentiBersaglio.get(subcommentiBersaglio.size() - 1) != subcommento) {
            throw new IllegalStateException("Verifica preliminare del benchmark fallita.");
        }
        subcommentiBersaglio.remove(subcommentiBersaglio.size() - 1);
    }

    @Benchmark
    public Subcommento aggiungiSubcommentoACommento() {
        Subcommento risultato = service.aggiungiSubcommentoACommento(idCommento, subcommento);
        // Rimozione O(1) dell'elemento aggiunto: la lista non cresce tra le invocazioni.
        subcommentiBersaglio.remove(subcommentiBersaglio.size() - 1);
        return risultato;
    }

    @TearDown(Level.Trial)
    public void verificaStato() {
        if (subcommentiBersaglio.size() != dimensioneIniziale) {
            throw new IllegalStateException("Stato del dataset alterato durante il benchmark.");
        }
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
