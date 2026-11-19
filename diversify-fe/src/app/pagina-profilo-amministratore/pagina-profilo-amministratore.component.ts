import { Component, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-pagina-profilo-amministratore',
  templateUrl: './pagina-profilo-amministratore.component.html',
  styleUrls: ['./pagina-profilo-amministratore.component.css'],
})
export class PaginaProfiloAmministratoreComponent {
  @Output() setActiveComponent = new EventEmitter<string>();  // Emetti evento al componente padre
  activeLink: string | null = null;

  setActiveLink(event: MouseEvent): void {
    event.preventDefault();  // Previene il comportamento di default del link
    const clickedLink = (event.target as HTMLAnchorElement).textContent?.toLowerCase().replace(/\s/g, '');
    this.activeLink = clickedLink || null;
    if (clickedLink) {
      this.setActiveComponent.emit(clickedLink);  // Emetti l'evento con il componente selezionato
    }
  }
}
