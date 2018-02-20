# ActivityRing
A Circular Progress UI which is based on Apples  Activity Rings

#Contents

- [Installation](#installation)
- [Usage](#usage)
  - [Single Ring](#single-ring)
  - [Staggered Rings](#staggered-ring)
- [Customisation](#customisation)
  - [ActivityRing](#activityring)
  - [ActivityRingContainer](#activityringcontainer)
- [License](#license)


# Installation
In root `build.gradle`
```groovy
allprojects {
  repositories {
	  ...
		maven { url 'https://jitpack.io' }
	}
}
```

In project `build.gradle`
```groovy
dependencies {
  implementation 'com.github.ABausG:ActivityRing:v1.0'
}
```

# Usage

You can use the Activity Ring as a single Widget or use the Container to create staggered Ring Layout
To change the Progress of a Ring simply set its `progress` value to the desired `Float` value. If the value is >1 the Ring will draw the the progress above a solid Ring

## Single Ring
Simply add a [ActivityRing](activityring/src/main/java/es/antonborri/activityring/ActivityRing.kt) to your Layout via xml or programmatically

## Staggered Ring
Add a [ActivityRingContainer](activityring/src/main/java/es/antonborri/activityring/ActivityRingContainer.kt) to your layout. To put rings in the Container call `addRing(ring)` on the container.

# Customisation
You can style the Rings and the Container using both xml and Code

Add this for all attributes you want to change to the xml Layout
```xml
app:attribute="value"
```

or call this on the Objects you want to style
```kotlin
activityRing.attribute = value
activityRingContainer.attribute = value
```

## ActivityRing

attribute   | type    |Default | effect|
------------|---------|--------|--------
color       |Color    |accentColor|Changes the color of the Ring
emptyOpacity|Float    |0.15    |Changes the opacity of the empty ring in the background
strokeWidth |Dimension|32dp    |Changes the width of the Stroke
iconColor   |Color    |Black   |Changes the Icon color
icon        |Drawable |null    |Changes the Icon
progress    |Float    |0       |Changes the Progress that is filled
showIcon    |Boolean  |true    |Toggle the Icon on/off


## ActivityRingContainer
attribute   | type    |Default | effect|
------------|---------|--------|--------
ringMargin  |Dimension|2dp     |Space between Rings
showIcon    |Boolean  |true    |Toggles Icons On/Off for all Rings in View

The Container will change the Strokewidth of all Children automatically to have enough space for every Ring

# License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
