import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'front-end';

  get isSuggestionsShown() {
    var element = document.querySelector('.spotify-iframe');
    if (element?.classList.contains("d-none")) {
      return false;
    }else {
      return true;
    }
  }

  generateSong(json: any) {
    return new Song(json);
  }

  get apiResponse() {
    return globalVars.apiResponse;
  }
}

export class Song {
  json : any;

  constructor(json: any) {
    this.json = json;
  }

  get songName() {
    return this.json["name"];
  }

  get albumName() {
    return this.json["album"]["name"];
  }

  get albumCover() {
    return this.json["album"]["images"][2].url; 
  }

  get artistName() {
    return this.json["artists"][0]["name"];
  }

  get albumLink() {
    return this.json["album"]["external_urls"]["spotify"]; 
  }

  get songLink() {
    return this.json["external_urls"]["spotify"];
  }

  get artistLink() {
    return this.json["artists"][0]["external_urls"]["spotify"];
  }

  get previewLink() {
    return this.json["preview_url"];
  }
}

class GlobalVariables {
  songMode : boolean  = false;
  apiResponse : any = [];

  setSongMode(input : boolean) {
    this.songMode = input;
  }

  setApiResponse(response : any) {
    this.apiResponse = response;
  }
}

export const globalVars = new GlobalVariables();