import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.timesPerSecond
import com.soywiz.korev.Key
import com.soywiz.korge.*
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.time.delay
import com.soywiz.korge.time.frameBlock
import com.soywiz.korge.view.*
import com.soywiz.korge.view.tiles.BaseTileMap
import com.soywiz.korge.view.tiles.TileSet
import com.soywiz.korge.view.tiles.tileMap
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.*
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korio.async.launch
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.SizeInt
import kotlin.reflect.KClass


suspend fun main() = Korge(Korge.Config(module = ConfigModule))



object ConfigModule : Module() {
	override val bgcolor = Colors["#2b2b2b"]
	override val size = SizeInt(480, 270)
	override val mainScene : KClass<out Scene> = Scene1::class

	override suspend fun AsyncInjector.configure() {
		mapPrototype { Scene1() }
		mapPrototype { Scene2() }
		mapPrototype { Scene3() }
	}

}

class Scene1: Scene() {
	override suspend fun Container.sceneInit() {
		val bitmap = resourcesVfs["start.png"].readBitmap()
		val image = image(bitmap).scale(0.3).centerOnStage()

		image.onClick {
			sceneContainer.changeTo<Scene2>()
		}
	}
}

class Scene2 : Scene() {
	override suspend fun Container.sceneInit() {

		// tileable background
		val tileset = TileSet(bitmap("assembly.png")
			.toBMP32()
			.scaleLinear(1.0, 1.0).slice(), 480, 270)
		val tilemap = tileMap(
			Bitmap32(1,1),
			repeatX = BaseTileMap.Repeat.REPEAT,
			tileset = tileset)

		launchImmediately {
			frameBlock(144.timesPerSecond) {
				while (true) {
					tilemap.x -= 0.5
					frame()
				}
			}
		}

		val cube = solidRect(10.0, 10.0, Colors.GOLD).xy(300, 200)
		val cube2 = solidRect(48.0, 48.0, Colors.RED).xy(20, 200)
		addChild(cube)

		val spriteMapRun: Bitmap = resourcesVfs["dog_run.png"].readBitmap()
		val spriteMapDeath: Bitmap = resourcesVfs["dog_death.png"].readBitmap()

		val runAnimation = SpriteAnimation(
			spriteMap = spriteMapRun,
			spriteWidth = 48,
			spriteHeight = 48,
			marginLeft = 0,
			marginTop = 0,
			columns = 6,
			rows = 1,
			offsetBetweenColumns = 0,
			offsetBetweenRows = 0
		)
		val deathAnimation = SpriteAnimation(
			spriteMap = spriteMapDeath,
			spriteWidth = 48,
			spriteHeight = 48,
			marginLeft = 0,
			marginTop = 0,
			columns = 4,
			rows = 1,
			offsetBetweenColumns = 0,
			offsetBetweenRows = 0
		)

		val dog: Sprite = sprite(runAnimation).xy(20, 200)
		dog.playAnimationLooped(spriteDisplayTime = 60.milliseconds)

		cube.addUpdater {
			x -= 2
		}

		dog.addUpdater {
			if(collidesWith(cube)) {
				playAnimation(deathAnimation)
				stopAnimation()
				launch {
					delay(TimeSpan(800.0))
					sceneContainer.changeTo<Scene3>()
				}
			}

			if(views.input.keys[Key.UP]) {
				dog.y -= 3
			}

			if(views.input.keys[Key.DOWN]) {
				dog.y += 3
			}

			if(views.input.keys[Key.LEFT]) {
				dog.x -= 3
			}

			if(views.input.keys[Key.RIGHT]) {
				dog.x += 3
			}
		}
	}

}

suspend fun bitmap(path: String) = resourcesVfs[path].readBitmap()

class Scene3 : Scene() {
	override suspend fun Container.sceneInit() {
		val bitmap = resourcesVfs["game_over.png"].readBitmap()
		val image = image(bitmap).scale(0.3).centerOnStage()

		image.onClick {
			sceneContainer.changeTo<Scene2>()
		}
	}
}