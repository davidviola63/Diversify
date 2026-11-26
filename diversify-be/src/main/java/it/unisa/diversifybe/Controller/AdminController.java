package it.unisa.diversifybe.Controller;

import it.unisa.diversifybe.Model.CampagnaCrowdFunding;
import it.unisa.diversifybe.Model.Forum;
import it.unisa.diversifybe.Service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/ban-user/{username}")
    public ResponseEntity<Void> banUser(@PathVariable final String username) {
        adminService.banUser(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forums")
    public ResponseEntity<Forum> createForum(@RequestBody final Forum forum,
                                             @RequestParam final boolean ruolo) {
        final Forum createdForum = adminService.createForumAsAdmin(forum, ruolo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdForum);
    }

    @PutMapping("/forums/{id}")
    public ResponseEntity<Forum> updateForum(@PathVariable final String id,
                                             @RequestBody final Forum updatedForum,
                                             @RequestParam final boolean ruolo) {
        final Forum forum = adminService.updateForumAsAdmin(id, updatedForum, ruolo);
        return ResponseEntity.ok(forum);
    }

    @PostMapping("/campaigns")
    public ResponseEntity<CampagnaCrowdFunding> createCampaign(@RequestBody final CampagnaCrowdFunding campagna,
                                                               @RequestParam final boolean ruolo) {
        final CampagnaCrowdFunding createdCampaign = adminService.createCampaignAsAdmin(campagna, ruolo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCampaign);
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<CampagnaCrowdFunding> updateCampaign(@PathVariable final String id,
                                                               @RequestBody final CampagnaCrowdFunding updatedCampagna,
                                                               @RequestParam final boolean ruolo) {
        final CampagnaCrowdFunding campaign = adminService.updateCampaignAsAdmin(id, updatedCampagna, ruolo);
        return ResponseEntity.ok(campaign);
    }
}