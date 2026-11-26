package it.unisa.diversifybe.Benchmark.JMH;

import it.unisa.diversifybe.Model.CampagnaCrowdFunding;
import it.unisa.diversifybe.Model.Paese;
import it.unisa.diversifybe.Repository.CampagnaCrowdFundingRepository;
import it.unisa.diversifybe.Repository.PaeseRepository;
import it.unisa.diversifybe.Service.CampagnaCrowdFundingService;

import org.openjdk.jmh.annotations.*;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// Misura getCampagnaByIdCampagna() invocando il metodo originale del service.
// Valuta la ricerca in memoria con la campagna collocata in ultima posizione.
// Dataset deterministici: SMALL (100), MEDIUM (1000), LARGE (5000).
// Repository simulati con Mockito; overhead del mock incluso, MongoDB escluso.

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@Threads(1)
public class GetCampagnaByIdCampagnaTestJMH {

    @Param({"SMALL", "MEDIUM", "LARGE"})
    public String datasetSize;

    private CampagnaCrowdFundingService service;
    private String idCampagna;

    @Setup(Level.Trial)
    public void setup() {

        int numeroPaesi;
        int campagnePerPaese;

        switch (datasetSize) {
            case "SMALL" -> {
                numeroPaesi = 10;
                campagnePerPaese = 10;
            }
            case "MEDIUM" -> {
                numeroPaesi = 50;
                campagnePerPaese = 100;
            }
            case "LARGE" -> {
                numeroPaesi = 200;
                campagnePerPaese = 500;
            }
            default -> throw new IllegalArgumentException(
                    "Dataset non valido: " + datasetSize
            );
        }

        PaeseRepository paeseRepository = Mockito.mock(
                PaeseRepository.class,
                Mockito.withSettings().stubOnly()
        );

        CampagnaCrowdFundingRepository campagnaRepository = Mockito.mock(
                CampagnaCrowdFundingRepository.class,
                Mockito.withSettings().stubOnly()
        );

        List<Paese> paesi = new ArrayList<>(numeroPaesi);

        for (int i = 0; i < numeroPaesi; i++) {

            Paese paese = new Paese();
            paese.setIdPaese("paese-" + i);

            List<CampagnaCrowdFunding> campagne =
                    new ArrayList<>(campagnePerPaese);

            for (int j = 0; j < campagnePerPaese; j++) {

                CampagnaCrowdFunding campagna =
                        new CampagnaCrowdFunding();

                campagna.setIdCampagna("campagna-" + i + "-" + j);

                campagne.add(campagna);
            }

            paese.setCampagneCrowdfunding(campagne);
            paesi.add(paese);
        }

        idCampagna = "campagna-"
                + (numeroPaesi - 1)
                + "-"
                + (campagnePerPaese - 1);

        Mockito.when(paeseRepository.findAll())
                .thenReturn(paesi);

        service = new CampagnaCrowdFundingService(
                paeseRepository,
                campagnaRepository
        );

        Optional<CampagnaCrowdFunding> risultato =
                service.getCampagnaByIdCampagna(idCampagna);

        if (risultato.isEmpty()
                || !idCampagna.equals(risultato.get().getIdCampagna())) {

            throw new IllegalStateException(
                    "Verifica preliminare del benchmark fallita."
            );
        }
    }

    @Benchmark
    public Optional<CampagnaCrowdFunding> getCampagnaByIdCampagna() {

        return service.getCampagnaByIdCampagna(idCampagna);
    }
}