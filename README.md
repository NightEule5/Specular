# Specular

Specular is a Kotlin library that abstracts various tasks involving reflection. Currently this only includes object models, but it may be expanded to other areas in the future.

This project started as part of a Minecraft mod I'm working on. I wanted a way to serialize and deserialize NBT to classes automatically, and found [AutoSave](https://github.com/SleepyTrousers/AutoSave), an existing project for that purpose, to be unsuitable. So, as a foundation, I built Specular (at first it was called "Mirror").

## Using it

To use it, clone this repository and run the `publishSpecularPublicationToMavenLocal` task. Then you can depend on it in Gradle like this:

```groovy
repositories {
	mavenLocal()
}

dependencies {
	implementation "strixpyrr.specular:Specular:0.5"
}
```

Keep in mind that it's nowhere near stable. I recommend against using it in production.
