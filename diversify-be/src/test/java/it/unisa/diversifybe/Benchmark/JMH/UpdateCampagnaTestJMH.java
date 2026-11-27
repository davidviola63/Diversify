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
import java.util.concurrent.TimeUnit;

// Misura updateCampagna() invocando il metodo originale del service.
// Valuta la ricerca e l'aggiornamento in memoria dell'ultima campagna, ripristinata prima di ogni invocazione.
// Dataset deterministici: SMALL (100), MEDIUM (5000), LARGE (100000) campagne; aggiornamento di titolo e descrizione.
// Repository simulati con Mockito; overhead dei mock incluso, MongoDB escluso.

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@Threads(1)
public class UpdateCampagnaTestJMH {

    @Param({"SMALL", "MEDIUM", "LARGE"})
    public String datasetSize;

    private CampagnaCrowdFundingService service;
    private String idCampagna;

    private CampagnaCrowdFunding campagnaOriginale;
    private CampagnaCrowdFunding updatedCampagna;

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
                campagna.setTitolo("Titolo originale");
                campagna.setDescrizione("Descrizione originale");
                campagna.setPaese("paese-" + i);

                campagne.add(campagna);
            }

            paese.setCampagneCrowdfunding(campagne);
            paesi.add(paese);
        }

        Paese paeseTarget = paesi.getLast();

        campagnaOriginale =
                paeseTarget.getCampagneCrowdfunding().getLast();

        idCampagna = campagnaOriginale.getIdCampagna();

        updatedCampagna = new CampagnaCrowdFunding();

        updatedCampagna.setIdCampagna(idCampagna);
        updatedCampagna.setTitolo("Titolo aggiornato");
        updatedCampagna.setDescrizione("Descrizione aggiornata");
        updatedCampagna.setPaese(campagnaOriginale.getPaese());

        Mockito.when(paeseRepository.findAll())
                .thenReturn(paesi);

        Mockito.when(paeseRepository.save(Mockito.any(Paese.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service = new CampagnaCrowdFundingService(
                paeseRepository,
                campagnaRepository
        );

        CampagnaCrowdFunding risultato =
                service.updateCampagna(idCampagna, updatedCampagna);

        if (risultato == null
                || !idCampagna.equals(risultato.getIdCampagna())
                || !updatedCampagna.getTitolo().equals(risultato.getTitolo())
                || !updatedCampagna.getDescrizione().equals(risultato.getDescrizione())) {

            throw new IllegalStateException(
                    "Verifica preliminare del benchmark fallita."
            );
        }

        ripristinaCampagna();
    }

    @Setup(Level.Invocation)
    public void ripristinaCampagna() {

        campagnaOriginale.setTitolo("Titolo originale");
        campagnaOriginale.setDescrizione("Descrizione originale");
    }

    @Benchmark
    public CampagnaCrowdFunding updateCampagna() {

        return service.updateCampagna(idCampagna, updatedCampagna);
    }
}