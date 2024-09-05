<h1 align="center">🦋 Foreground 🦋</h1>

A simple [BlueSky](https://bsky.app) wrapper that allows to create automations and bots for the platform.

Current version is `1.0.1`

## Installation

Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    implementation("me.hechfx:foreground-core:$version")
}
```

## Usage

Listening for events

```kotlin
fun main() {
    val client = ForegroundClient {
        identifier = "your-identifier.bsky.app"
        password = "your-password"
    }
    
    client.on<CommitEvent> {
        if (content != null) {
            println("new commit received; type: ${content!!::class.simpleName} from $author")
            
            when (content) {
                is ContentType.LikeContent -> {
                    val like = content as ContentType.LikeContent

                    println("New like to ${like.subject.prettyURI}")
                }
                
                is ContentType.FollowContent -> {
                    val follow = content as ContentType.FollowContent

                    println("New follow to ${follow.subjectURI}")
                }
            }
        }
    }
    
    client.awaitConnect()
}
```

Using the API

```kotlin
val client = ForegroundClient {
    identifier = "your-identifier.bsky.app"
    password = "your-password"
}

// creating a simple post
client.api.createPost {
    text = "Hello, world!"
}

// replying to some post
val post = client.api.retrievePostByURI("at://profile/post_lexicon/post")

if (post != null) {
    client.api.createPost {
        text = "Replying to post"

        replyTo(post.cid, post.uri)
    }
}
```

## Contributing

Feel free to open issues or submit pull requests if you find any bugs or have feature requests.

## License

This project is licensed under the MIT License.
