# Simple Avrohugger

Extremely simplified version of Julian Peeters's  [sbt-avrohugger](https://github.com/julianpeeters/sbt-avrohugger) 
that doesn't make any assumptions.

Right now only supports generating specific records.

## Installation

Add this to `plugins.sbt`:

```
addSbtPlugin("com.mariussoutier.sbt" % "sbt-simple-avrohugger" % "0.9.1")
```

## Usage

First, enable the plugin on the project you want to use it with.

```scala
import com.mariussoutier.sbt.AvroGeneratorKeys.Avro
lazy val root = project(...)
  .enablePlugins(com.mariussoutier.sbt.AvroGenerator)
  .settings(
    sourceDirectory in Avro := target.value / "avro"
  )
```

Then use `generateSpecific` by adding it to a generator, for example:

```scala
import com.mariussoutier.sbt.AvroGeneratorKeys
sourceGenerators in Compile += AvroGeneratorKeys.generateSpecific
```

To chain it with other plugins, e.g. my [sbt-unpack](https://github.com/mariussoutier/sbt-unpack) plugin, do this:

```scala
import com.mariussoutier.sbt.UnpackKeys
import com.mariussoutier.sbt.AvroGeneratorKeys
sourceGenerators in Compile += Def.sequential(UnpackKeys.unpackJars, AvroGeneratorKeys.generateSpecific)
```

Filter files by using sbt's include or exclude filter.

```scala
import com.mariussoutier.sbt.AvroGeneratorKeys.Avro
includeFilter in Avro := "*.avdl" | "*.avsc"
```
