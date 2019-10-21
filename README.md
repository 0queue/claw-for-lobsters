# Claw for Lobsters

A little personal project to play around with different Android libraries
and what not.  Connects to [Lobste.rs](https://lobste.rs), a news site

## Structure

Played around with a modular architecture, although for now there is a
low number of features, and only two real library modules (:core, :lib-lobsters)

In any case, the structure as of the beginning of October 2019 is:

```
     +--> :feature-front-page
     |                   |
     |                   V       +---> :lib-lobsters (api)
:app +----------------> :core ---|
     |                   ^       +---> :lib-navigation (api)
     |                   |       
     +--> :feature-comments
```

Where :app is pretty much empty except for the manifest,
features contain Conductor controllers, :core contains
Android pieces that are useful in all (both) features,
:lib-lobsters contains the access to the website
(cached in a database), and :lib-navigation really doesn't
do much besides put all the reflection needed for modular
navigation in one place

## Libraries/technologies used

Hopefully I remembered them all

- Kotlin
- Coroutines
- Conductor
- SQLDelight
- Retrofit
- Leak Canary
- Material Components
- Gradle w/ Kotlin Script and buildSrc module
- Dagger 2
- Paging Architecture Component
- Coil
- Chrome custom tabs

## Future plans

- ~~Night mode~~
- ~~Better comment rendering (HtmlCompat.fromHtml(...) is really inadequate)~~
  (URLSpan has been replaced)
- ~~Settings page~~
- Github releases, maybe
- Feature to see profile pages
- Tag filtering
