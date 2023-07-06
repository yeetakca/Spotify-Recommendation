package com.spotify.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class SpotifyService {

    private static final String SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1";
    private static final String CLIENT_ID = "a71823ac4def4f24949a429bf9f056df";
    private static final String CLIENT_SECRET = "58b06a61d6b74215be2fbb1eb45aa32b";
    private static final String AUTHORIZATION_URL = "https://accounts.spotify.com/api/token";
    private static List<String> trackstoExclude = new ArrayList<>();


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SpotifyService() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);
        URI uri;
        try {
            uri = new URI(AUTHORIZATION_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid authorization URL", e);
        }

        ResponseEntity<SpotifyTokenResponse> response = restTemplate.exchange(uri, HttpMethod.POST, request, SpotifyTokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            SpotifyTokenResponse tokenResponse = response.getBody();
            if (tokenResponse != null) {
                return tokenResponse.getAccessToken();
            }
        }

        throw new IllegalStateException("Failed to retrieve access token from Spotify API");
    }

//    public String getTrack(String trackId) {
//        String endpoint = SPOTIFY_API_BASE_URL + "/tracks/" + trackId;
//        return performRequest(endpoint);
//    }

    public String getPlaylist(String playlistId) {
        String endpoint = SPOTIFY_API_BASE_URL + "/playlists/" + playlistId;
        return performRequest(endpoint, trackstoExclude);
    }

    public String getFeatures(String trackId) {
        String endpoint = SPOTIFY_API_BASE_URL + "/audio-features/" + trackId;
        return performRequest(endpoint, trackstoExclude);
    }

    public String getTrackRecs(String trackId) {
        String features = getFeatures(trackId);
        JsonNode node;
        try {
            node = objectMapper.readTree(features);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        StringBuilder endpointBuilder = new StringBuilder(SPOTIFY_API_BASE_URL + "/recommendations");
        endpointBuilder.append("?limit=15")
                .append("&seed_tracks=").append(trackId)
                .append("&target_acousticness=").append(node.get("acousticness"))
                .append("&target_danceability=").append(node.get("danceability"))
                .append("&target_energy=").append(node.get("energy"))
                .append("&target_instrumentalness=").append(node.get("instrumentalness"))
                .append("&target_liveness=").append(node.get("liveness"))
                .append("&target_loudness=").append(node.get("loudness"))
                .append("&target_speechiness=").append(node.get("speechiness"))
                .append("&target_tempo=").append(node.get("tempo"))
                .append("&target_valence=").append(node.get("valence"));

        String endpoint = endpointBuilder.toString();
        trackstoExclude.add(trackId);
        return performRequest(endpoint, trackstoExclude);
    }

    public String getPlaylistRec(String playlistId) {
        String playlist = getPlaylist(playlistId);
        JsonNode playlistNode, featuresNode;
        List<String> trackIds = new ArrayList<>();
        List<String> artists = new ArrayList<>();
        float acousticness = 0, danceability = 0, energy = 0, instrumentalness = 0, liveness = 0, loudness = 0,
                speechiness = 0, tempo = 0, valence = 0;

        try {
            playlistNode = objectMapper.readTree(playlist);
            JsonNode tracksNode = playlistNode.get("tracks").get("items");
            for (JsonNode track : tracksNode) {
                String trackId = track.get("track").get("id").asText();
                trackIds.add(trackId);
                String artistId = track.get("track").get("artists").get(0).get("id").asText();
                artists.add(artistId);
            }
            for (String trackId : trackIds) {
                String features = getFeatures(trackId);
                featuresNode = objectMapper.readTree(features);
                acousticness += featuresNode.get("acousticness").floatValue();
                danceability += featuresNode.get("danceability").floatValue();
                energy += featuresNode.get("energy").floatValue();
                instrumentalness += featuresNode.get("instrumentalness").floatValue();
                liveness += featuresNode.get("liveness").floatValue();
                loudness += featuresNode.get("loudness").floatValue();
                speechiness += featuresNode.get("speechiness").floatValue();
                tempo += featuresNode.get("tempo").floatValue();
                valence += featuresNode.get("valence").floatValue();
            }
            int numTracks = trackIds.size();
            acousticness /= numTracks;
            danceability /= numTracks;
            energy /= numTracks;
            instrumentalness /= numTracks;
            liveness /= numTracks;
            loudness /= numTracks;
            speechiness /= numTracks;
            tempo /= numTracks;
            valence /= numTracks;

            List<String> commonArtists = findCommonArtists(artists);

            StringBuilder endpointBuilder = new StringBuilder(SPOTIFY_API_BASE_URL + "/recommendations");
            endpointBuilder.append("?limit=15");

            if (!commonArtists.isEmpty()) {
                int numArtists = Math.min(5, commonArtists.size());
                for (int i = 0; i < numArtists; i++) {
                    endpointBuilder.append("&seed_artists=").append(commonArtists.get(i));
                }
            }

            endpointBuilder.append("&target_acousticness=").append(acousticness)
                    .append("&target_danceability=").append(danceability)
                    .append("&target_energy=").append(energy)
                    .append("&target_instrumentalness=").append(instrumentalness)
                    .append("&target_liveness=").append(liveness)
                    .append("&target_loudness=").append(loudness)
                    .append("&target_speechiness=").append(speechiness)
                    .append("&target_tempo=").append(tempo)
                    .append("&target_valence=").append(valence);

            String endpoint = endpointBuilder.toString();
            trackstoExclude.addAll(trackIds);
            return performRequest(endpoint, trackstoExclude);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> findCommonArtists(List<String> artists) {
        HashMap<String, Integer> artistCount = new HashMap<>();

        for (String artist : artists) {
            artistCount.put(artist, artistCount.getOrDefault(artist, 0) + 1);
        }

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(artistCount.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        List<String> commonArtists = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            commonArtists.add(entry.getKey());
            count++;
            if (count == Math.min(5, sortedEntries.size())) {
                break;
            }
        }

        return commonArtists;
    }

    private String performRequest(String endpoint, List<String> trackstoExclude) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> request = new HttpEntity<>(headers);
        JsonNode recNode;

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        if (trackstoExclude != null && !trackstoExclude.isEmpty() && response.getStatusCode().is2xxSuccessful()) {
            try {
                recNode = objectMapper.readTree(response.getBody());
                System.out.println(trackstoExclude);
                for (int i = 0; i < recNode.get("tracks").size(); i++) {
                    System.out.println(recNode.get("tracks").get(i).get("id").toString().replaceAll("\"", ""));
                }
                int size = recNode.get("tracks").size();
                int index = -1;
                for (int i = 0; i < size; i++) {
                    for (String s : trackstoExclude) {
                        if (s.equals(recNode.get("tracks").get(i).get("id").toString().replaceAll("\"", ""))) {
                            index = i;
                        }
                    }
                }
                if (index != -1) {
                    ((ArrayNode) recNode.get("tracks")).remove(index);
                    System.out.println(index + "silindi");
                }
                for (int i = 0; i < recNode.get("tracks").size(); i++) {
                    System.out.println(recNode.get("tracks").get(i).get("id"));
                }

                trackstoExclude.clear();
                return recNode.toString();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else if (response.getStatusCode().is2xxSuccessful() && trackstoExclude.isEmpty()) {
            trackstoExclude.clear();
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve track information");
        }
    }
}
