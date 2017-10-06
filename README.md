# Simple Avrohugger

Extremely simplified version of Julian's sbt-avrohugger that doesn't make any assumptions.

Right now only supports generating specific records.

## Installation

Add this to `plugins.sbt`:

```
resolvers += Resolver.bintrayIvyRepo("mariussoutier", "sbt-plugins")
addSbtPlugin("com.mariussoutier.sbt" % "sbt-simple-avrohugger" % "0.9.0")
```

## Usage

First, enable the plugin on the project you want to use it with.

```scala
lazy val root = project(...)
  .enablePlugins(com.mariussoutier.sbt.AvroGenerator)
  .settings(
    `sourceDirectory in AvroGeneratorKeys.Avro := target.value / "avro" `
  )
```

Then use `generateSpecific` by adding it to a generator, for example:

```scala
sourceGenerators in Compile += Def.sequential(UnpackKeys.unpackJars, AvroGeneratorKeys.generateSpecific)
```
