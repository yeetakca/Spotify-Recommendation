import { Component } from '@angular/core';
import { globalVars } from '../app.component';

@Component({
  selector: 'app-nav-bar',
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.scss']
})
export class NavBarComponent {
  toggleSearchBar() {
    document.querySelector(".search-bar-container")?.classList.toggle("d-none");
  }

  toggleMode() {
    globalVars.setSongMode(!globalVars.songMode);
    (<HTMLInputElement> document.querySelector(".search-bar-container span input")).value = "";
  }

  get songMode() {
    return globalVars.songMode;
  }
}
