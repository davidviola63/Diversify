import { Routes } from '@angular/router';
import { ForumAccessGuard } from './guards/forum-access.guard';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./homepage/homepage.component').then(m => m.HomepageComponent)
  },
  {
    path: 'post/:postId/:selectedForumId',
    loadComponent: () =>
      import('./post/post.component').then(m => m.PostComponent),
    canActivate: [ForumAccessGuard]
  },
  {
    path: 'loggato',
    loadComponent: () =>
      import('./login-form/login-form.component').then(m => m.LoginFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'registrato',
    loadComponent: () =>
      import('./registrazione-form/registrazione-form.component').then(m => m.RegistrazioneFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'scheda-area-personale',
    loadComponent: () =>
      import('./scheda-area-personale/scheda-area-personale.component').then(m => m.SchedaAreaPersonaleComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'pagina-profilo-amministratore',
    loadComponent: () =>
      import('./pagina-profilo-amministratore/pagina-profilo-amministratore.component').then(m => m.PaginaProfiloAmministratoreComponent),
    canActivate: [AdminGuard]
  },
  {
    path: 'generale-amministratore',
    loadComponent: () =>
      import('./generale-amministratore/generale-amministratore.component').then(m => m.GeneraleAmministratoreComponent),
    canActivate: [AdminGuard]
  },
  {
    path: 'chi-siamo',
    loadComponent: () =>
      import('./chi-siamo/chi-siamo.component').then(m => m.ChiSiamoComponent)
  },
  {
    path: 'creapost',
    loadComponent: () =>
      import('./form-post/form-post.component').then(m => m.FormPostComponent),
    canActivate: [ForumAccessGuard]
  },
  {
    path: 'recupero-password',
    loadComponent: () =>
      import('./gestione-password-form/gestione-password-form.component').then(m => m.GestionePasswordFormComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'forum',
    loadComponent: () =>
      import('./forum/forum.component').then(m => m.ForumComponent),
    canActivate: [ForumAccessGuard]
  },
  {
    path: 'forum/:forumId',
    loadComponent: () =>
      import('./forum/forum.component').then(m => m.ForumComponent),
    canActivate: [ForumAccessGuard]
  },
  {
    path: 'campagne/:paese',
    loadComponent: () =>
      import('./campagna-crowdfunding/campagna-crowdfunding.component').then(m => m.CampagnaCrowdfundingComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'documentoinformativo/:paese',
    loadComponent: () =>
      import('./documento-informativo/documento-informativo.component').then(m => m.DocumentoInformativoComponent)
  },
  {
    path: 'donazione-campagna',
    loadComponent: () =>
      import('./donazione-campagna/donazione-campagna.component').then(m => m.DonazioneCampagnaComponent)
  }
];
