package it.unisa.diversifybe.Benchmark.JMH;

import it.unisa.diversifybe.Model.DocumentoInformativo;
import it.unisa.diversifybe.Model.Paese;
import it.unisa.diversifybe.Repository.DocumentoInformativoRepository;
import it.unisa.diversifybe.Repository.PaeseRepository;
import it.unisa.diversifybe.Service.DocumentoInformativoService;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Sottopone a benchmark DocumentoInformativoService.findByIdPaese(String).
// Misura la scansione dei paesi e la copia dei documenti del paese corrispondente.
// Dataset sintetico deterministico: SMALL (100), MEDIUM (1000), LARGE (5000) paesi; 5 documenti trovati.
// PaeseRepository e DocumentoInformativoRepository sono mockati; database, rete e Spring sono esclusi.
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@Threads(1)
public class FindByIdPaeseTestJMH {

    @Param({"SMALL", "MEDIUM", "LARGE"})
    public String datasetSize;

    private DocumentoInformativoService service;
    private String idPaese;

    @Setup
    public void setup() {
        int numeroPaesi = switch (datasetSize) {
            case "SMALL" -> 100;
            case "MEDIUM" -> 1_000;
            case "LARGE" -> 5_000;
            default -> throw new IllegalArgumentException("Dataset non valido: " + datasetSize);
        };

        idPaese = "paese-" + (numeroPaesi - 1);
        List<Paese> paesi = new ArrayList<>(numeroPaesi);

        for (int i = 0; i < numeroPaesi; i++) {
            Paese paese = new Paese();
            paese.setIdPaese("paese-" + i);
            paese.setDocumentiInformativi(i == numeroPaesi - 1
                    ? creaDocumenti(idPaese)
                    : List.of());
            paesi.add(paese);
        }

        PaeseRepository paeseRepository = Mockito.mock(
                PaeseRepository.class,
                Mockito.withSettings().stubOnly()
        );
        DocumentoInformativoRepository documentoInformativoRepository = Mockito.mock(
                DocumentoInformativoRepository.class,
                Mockito.withSettings().stubOnly()
        );
        Mockito.when(paeseRepository.findAll()).thenReturn(paesi);

        service = new DocumentoInformativoService(
                documentoInformativoRepository,
                paeseRepository
        );

        List<DocumentoInformativo> risultato = service.findByIdPaese(idPaese);
        if (risultato.size() != 5) {
            throw new IllegalStateException("Verifica preliminare del benchmark fallita.");
        }
    }

    @Benchmark
    public List<DocumentoInformativo> findByIdPaese() {
        return service.findByIdPaese(idPaese);
    }

    private static List<DocumentoInformativo> creaDocumenti(String idPaese) {
        List<DocumentoInformativo> documenti = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            DocumentoInformativo documento = new DocumentoInformativo();
            documento.setIdDocumentoInformativo("documento-" + i);
            documento.setIdPaese(idPaese);
            documenti.add(documento);
        }
        return documenti;
    }
}
