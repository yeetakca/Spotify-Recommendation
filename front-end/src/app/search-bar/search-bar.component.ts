import { Component, ViewChild } from '@angular/core';
import { globalVars } from '../app.component';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.scss']
})
export class SearchBarComponent {
  @ViewChild('searchPlaylist') searchPlaylist: any;

  ip : string = "10.10.6.31";
  port : string = "8080";

  inputValue : string = "";

  constructor(private http: HttpClient) {
    
  }

  onKey(event: any) {
    this.inputValue = event.target.value;
  }

  onBlur(event: any) {
    setTimeout(() => {
      this.inputValue = event.target.value;
    }, 100);
  }

  get id() {
    return this.inputValue.split("/").slice(-1)[0].split("?")[0];
  }

  get songMode() {
    return globalVars.songMode;
  }

  suggest() {
    if (this.inputValue.indexOf("track") != -1) {
      globalVars.setSongMode(true);
    }else {
      globalVars.setSongMode(false);
    }

    document.querySelector(".search-bar-container")?.classList.add("d-none");
    if (document.querySelector(".spotify-iframe")?.classList.contains("d-none")) {
      document.querySelector(".spotify-iframe")?.classList.remove("d-none");
    }
    if (this.songMode) {
      document.querySelector(".playlist-showcase")?.setAttribute("src", "https://open.spotify.com/embed/track/" + this.id + "?theme=0")
    }else {
      document.querySelector(".playlist-showcase")?.setAttribute("src", "https://open.spotify.com/embed/playlist/" + this.id + "?theme=0")
    }

    globalVars.setApiResponse([]);

    if (this.songMode) {
      this.http.get<any>(`http://${this.ip}:${this.port}/trackrec?id=${this.id}`).subscribe(data => {
        console.log(data);  
        globalVars.setApiResponse(data.tracks);
      });
    }else {
      this.http.get<any>(`http://${this.ip}:${this.port}/playlistrec?id=${this.id}`).subscribe(data => {
        console.log(data);    
        globalVars.setApiResponse(data.tracks);
      });
    }

    this.searchPlaylist.nativeElement.value = "";
    this.inputValue = "";
  }
}