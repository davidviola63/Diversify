package it.unisa.diversifybe.Benchmark.JMH;

import it.unisa.diversifybe.Controller.LikeController;
import it.unisa.diversifybe.Model.Commento;
import it.unisa.diversifybe.Model.Forum;
import it.unisa.diversifybe.Model.Post;
import it.unisa.diversifybe.Repository.ForumRepository;
import it.unisa.diversifybe.Service.CommentoService;
import it.unisa.diversifybe.Service.PostService;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Sottopone a benchmark LikeController.addLikeToCommento(String).
// Misura la scansione annidata di post e commenti, l'incremento del like, il salvataggio e la risposta HTTP.
// Dataset sintetico deterministico: SMALL (100), MEDIUM (1000), LARGE (5000) post con un commento ciascuno; target finale.
// ForumRepository è mockato; servizi, database, rete e contesto Spring sono esclusi.
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@Threads(1)
public class AddLikeToCommentoTestJMH {

    @Param({"SMALL", "MEDIUM", "LARGE"})
    public String datasetSize;

    private LikeController likeController;
    private String commentoId;
    private Commento commentoCercato;

    @Setup(Level.Trial)
    public void setup() {
        int numeroPost = switch (datasetSize) {
            case "SMALL" -> 100;
            case "MEDIUM" -> 1_000;
            case "LARGE" -> 5_000;
            default -> throw new IllegalArgumentException("Dataset non valido: " + datasetSize);
        };

        commentoId = "commento-" + (numeroPost - 1);
        List<Post> posts = new ArrayList<>(numeroPost);
        for (int i = 0; i < numeroPost; i++) {
            Commento commento = new Commento();
            commento.setIdCommento("commento-" + i);
            commento.setLike(0);

            Post post = new Post();
            post.setIdPost("post-" + i);
            post.setCommenti(List.of(commento));
            posts.add(post);

            if (i == numeroPost - 1) {
                commentoCercato = commento;
            }
        }

        Forum forum = new Forum();
        forum.setPost(posts);
        List<Forum> forums = List.of(forum);

        ForumRepository forumRepository = Mockito.mock(
                ForumRepository.class,
                Mockito.withSettings().stubOnly()
        );
        Mockito.when(forumRepository.findAll()).thenReturn(forums);

        likeController = new LikeController(
                Mockito.mock(CommentoService.class),
                Mockito.mock(PostService.class)
        );
        ReflectionTestUtils.setField(likeController, "forumRepository", forumRepository);

        likeController.addLikeToCommento(commentoId);
        if (commentoCercato.getLike() != 1) {
            throw new IllegalStateException("Verifica preliminare del benchmark fallita.");
        }
        commentoCercato.setLike(0);
    }

    @Setup(Level.Invocation)
    public void resetLike() {
        commentoCercato.setLike(0);
    }

    @Benchmark
    public ResponseEntity<?> addLikeToCommento() {
        return likeController.addLikeToCommento(commentoId);
    }
}
