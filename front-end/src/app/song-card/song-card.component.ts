import { Component, Input } from '@angular/core';
import { Song } from '../app.component';

@Component({
  selector: 'app-song-card',
  templateUrl: './song-card.component.html',
  styleUrls: ['./song-card.component.scss']
})
export class SongCardComponent {
  @Input() song!: Song;

  playSong() {
    var targetElement = document.querySelector(".preview-control-container");
    if (targetElement) {
      targetElement.innerHTML = "";
      var newElement = document.createElement("audio");
      newElement.controls = true;
      newElement.autoplay = true;
      var subElement = document.createElement("source");
      subElement.src = this.song.previewLink;
      subElement.type = "audio/mp3";
      newElement.append(subElement);
      targetElement.append(newElement);
    }
  }
}