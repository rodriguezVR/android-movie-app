# Movie App
An android application to explore movies from [The Movie Db](https://www.themoviedb.org/).

This app was developed following these frameworks / libraries:

- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/) 
    * [Paging](https://developer.android.com/topic/libraries/architecture/paging/) 
    * [Room](https://developer.android.com/topic/libraries/architecture/room)
    * [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
    * [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
- [Android Data Binding](https://developer.android.com/topic/libraries/data-binding/)
- [Retrofit](http://square.github.io/retrofit/) for REST API communication.
- [Picasso](http://square.github.io/picasso/) for image loading


### Getting Started

To build and test this project, first, you need to get an API key from The Movie Db (https://www.themoviedb.org/).
Then, replace `{Your_TheMovieDb_APIKey}` in file gradle.properties located in root folder.


gradle.properties
```
TMDb_ApiKey="{Your_TheMovieDb_APIKey}"
```


## App Sections / Layers

### 1.- Data

This sectio includes classes for data management. It has API datasources (server data), Room Persistence Library for DB abstraction (offline data), Shared preferences handler, and data converters. 

- Remote datasource used to fetch data from API server.<br><b>Classes involved:</b> MovieDataSource, MovieDataSourceFactory and TheMovieAPI.
- Room. The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.<br><b>Classes included:</b> MovieDatabase, MovieDao, MovieEntry and DateConverter.
- Shared Preferences. To keep user preferences.<br><b>Classes involved:</b> MoviePreferences.
- MovieRepository instanced as Singleton, to be used as main entrance to get access from both data sources, remote and local.

### 2.- Model

This section includes all data models used in App. These objects organize elements of data and standardizes how are they related to each other.

Data models classes are: Cast, Credits, Crew, Genre, Movie, MovieDetails, MovieResponse, Review, ReviewResponse, Video and VideoResponse.

You can noticed, all data models are related to Movie object (Information handle by this app is about Movies)


## License
Licensed under the Apache License, Version 2.0.