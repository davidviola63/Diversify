package it.unisa.diversifybe.Service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import it.unisa.diversifybe.Model.CampagnaCrowdFunding;
import it.unisa.diversifybe.Model.Forum;
import it.unisa.diversifybe.Model.Utente;
import it.unisa.diversifybe.Repository.UtenteRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final ForumService forumService;
    private final CampagnaCrowdFundingService campagnaService;
    private final UtenteRepository utenteRepository;

    public AdminService(UtenteRepository utenteRepository, ForumService forumService, CampagnaCrowdFundingService campagnaService) {
        this.utenteRepository = utenteRepository;
        this.forumService = forumService;
        this.campagnaService = campagnaService;
    }

    // Metodo per bannare un utente tramite username
    public void banUser(final String username) {
        final Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        utente.setBanned(true);
        utenteRepository.save(utente);
    }

    // Wrapper per creare un forum come amministratore
    public Forum createForumAsAdmin(final Forum forum, final boolean ruolo) {
        if (!ruolo) {
            throw new SecurityException("Solo gli amministratori possono creare un forum.");
        }

        return forumService.addForum(forum, true);
    }

    // Wrapper per modificare un forum come amministratore
    public Forum updateForumAsAdmin(final String id, final Forum updatedForum, final boolean ruolo) {
        if (!ruolo) {
            throw new SecurityException("Solo gli amministratori possono modificare un forum.");
        }

        return forumService.updateForum(id, updatedForum, true);
    }

    // Wrapper per creare una campagna come amministratore
    public CampagnaCrowdFunding createCampaignAsAdmin(final CampagnaCrowdFunding campagna, final boolean ruolo) {
        if (!ruolo) {
            throw new SecurityException("Solo gli amministratori possono creare una campagna.");
        }

        return campagnaService.createCampagna(campagna);
    }

    // Wrapper per modificare una campagna come amministratore
    public CampagnaCrowdFunding updateCampaignAsAdmin(final String id, final CampagnaCrowdFunding updatedCampagna, final boolean ruolo) {
        if (!ruolo) {
            throw new SecurityException("Solo gli amministratori possono modificare una campagna.");
        }

        return campagnaService.updateCampagna(id, updatedCampagna);
    }
}