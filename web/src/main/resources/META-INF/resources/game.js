const type = PIXI.utils.isWebGLSupported() ? "WebGL" : "canvas";
PIXI.settings.SPRITE_MAX_TEXTURES = Math.min(PIXI.settings.SPRITE_MAX_TEXTURES , 16);
const app = new PIXI.Application({
    width: window.innerWidth,
    height: window.innerHeight,
    antialias: true,
    resolution: 1,
    transparent: true,
    autoResize: true,
});

$("body").append(app.view);

let elements = {};

let players;
const explosion = [];

PIXI.loader
    .add('assets/images/players.json')
    .add('assets/images/explosion.json')
    .load(() => {
        players = PIXI.loader.resources["assets/images/players.json"].textures;

        // Explosion
        for (let i = 1; i <= 26; i++) {
            explosion.push(PIXI.Texture.fromFrame(`explosion${i}.png`));
        }

        app.renderer.render(app.stage);
    });
app.ticker.add(function (delta) {
    PIXI.tweenManager.update();
});

function explode(x, y) {
    const mc = new PIXI.extras.MovieClip(explosion);
    mc.position.x = x;
    mc.position.y = y;
    mc.anchor.x = 0.5;
    mc.anchor.y = 0.5;
    mc.loop = false;
    mc.animationSpeed = 0.6;
    mc.rotation = Math.random() * Math.PI;
    mc.scale.set(0.75 + Math.random() * 0.5);
    mc.onComplete = function() { app.stage.removeChild(mc); };
    mc.play();
    app.stage.addChild(mc);
}

function resetGame() {
    elements = {};
}

function removeGameObject(obj) {
    if (elements[obj.id]) {
        app.stage.removeChild(elements[obj.id].sprite);
        elements[obj.id] = null;
    }
}

function displayGameObject(obj) {
    if (!players) {
        return;
    }
    if (elements[obj.id]) {
        const el = elements[obj.id];
        if (el.spriteName != obj.sprite) {
            console.log(`Updating ${obj.id} with sprite ${obj.sprite}`)
            app.stage.removeChild(el.sprite);
            el.sprite = new PIXI.Sprite(players[obj.sprite + ".png"]);
            el.spriteName = obj.sprite;
            el.sprite.x = obj.x;
            el.sprite.y = obj.y;
            app.stage.addChild(el.sprite);
        } else {
            //console.log(`Updating ${obj.id}`)
            var tween = PIXI.tweenManager.createTween(el.sprite);
            tween.time = 1000;
            tween.easing = PIXI.tween.Easing.linear();
            tween.from({
                x: el.sprite.x,
                y: el.sprite.y
            });
            tween.to({
                x: obj.x,
                y: obj.y
            });
            tween.start();
        }
        if (obj.action === "explode") {
            console.log("explode!!!!!!!!!!!!!!!!!!!!!!")
            removeGameObject(obj);
            explode(obj.x, obj.y);
        }
    } else {
        const sprite = new PIXI.Sprite(players[obj.sprite + ".png"]);
        console.log(`Creating object ${obj.id} with sprite ${obj.sprite}`)
        sprite.x = obj.x;
        sprite.y = obj.y;
        elements[obj.id] = {
            spriteName: obj.sprite,
            sprite,
        }
        app.stage.addChild(sprite);
    }

}