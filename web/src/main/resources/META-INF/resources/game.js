const type = PIXI.utils.isWebGLSupported() ? "WebGL" : "canvas";
PIXI.settings.SPRITE_MAX_TEXTURES = Math.min(PIXI.settings.SPRITE_MAX_TEXTURES, 16);
const app = new PIXI.Application({
    width: 1024,
    height: 768,
    antialias: true,
    resolution: 1,
    transparent: true,
});

$("#container").append(app.view);
let ended = false;
let elements = {};

let players;
const explosion = [];

PIXI.loader
    .add('assets/images/players.json')
    .add('assets/images/explosion.json')
    .load(() => {
        players = PIXI.loader.resources["assets/images/players.json"].textures;
        /*
        //Show list of players
        const list = [];
        for(player in players) {
            list.push(`"${player.replace(".png", "")}"`);
        }
        console.log(list.join(", "));*/

        // Explosion
        for (let i = 1; i <= 26; i++) {
            explosion.push(PIXI.Texture.from(`explosion${i}.png`));
        }

        app.renderer.render(app.stage);
        app.ticker.add(delta => gameLoop(delta));
    });

app.ticker.add(function (delta) {
    PIXI.tweenManager.update();
});

function putInDirection(sprite, prevX, newX) {
    if (prevX > newX) {
        sprite.scale.x = -1;
    } else if (prevX < newX) {
        sprite.scale.x = 1;
    }
}

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
    mc.onComplete = function () { app.stage.removeChild(mc); };
    mc.play();
    app.stage.addChild(mc);
}

function resetGame() {
    elements = {};
    app.stage.removeChildren();
}

function removeGameObject(obj) {
    if (elements[obj.id]) {
        app.stage.removeChild(elements[obj.id].element);
        elements[obj.id] = null;
        delete elements[obj.id];
    }
}

function displayGameObject(obj) {
    if (!players || ended) {
        return;
    }
    if (elements[obj.id]) {
        const el = elements[obj.id];
        el.time = Date.now();
        if (!obj.sprite) {
            removeGameObject(obj);
        } else if (el.spriteName != obj.sprite) {
            console.log(`Updating ${obj.id} with sprite ${obj.sprite} at ${obj.x}, ${obj.y}`)
            app.stage.removeChild(el.element);
            if (obj.sprite === "explode") {
                explode(obj.x, obj.y);
            } else {
                el.sprite = new PIXI.Sprite(players[obj.sprite + ".png"]);
                el.spriteName = obj.sprite;
                el.element.x = obj.x;
                el.element.y = obj.y;
                el.label.text = obj.label;
                app.stage.addChild(el.element);
            }
        } else {
            //console.log(`Updating ${obj.id}`)
            putInDirection(el.sprite, el.sprite.x, obj.x);
            el.label.text = obj.label;
            if (!el.tween) {
                el.tween = PIXI.tweenManager.createTween(el.element);
                el.tween.time = 1000;
                el.tween.easing = PIXI.tween.Easing.linear();
            }
            el.tween.reset();
            el.tween.from({
                x: el.element.x,
                y: el.element.y
            });
            el.tween.to({
                x: obj.x,
                y: obj.y
            });
            el.tween.start();
        }

    } else {
        const element = new PIXI.Container();
        element.x = obj.x;
        element.y = obj.y;
        element.width = 64;
        element.height = 64;
        
        const sprite = new PIXI.Sprite(players[obj.sprite + ".png"]);
        console.log(`Creating object ${obj.id} with sprite ${obj.sprite} at ${obj.x}, ${obj.y}`)
        
        let label;
        if (obj.label) {
            label = new PIXI.Text(obj.label, { align: 'center' });
            label.width = 200;
            label.y = 30;
            label.x = -100;
            element.addChild(label);
        }

        sprite.anchor.x = 0.5;
        sprite.anchor.y = 0.5;
        sprite.scale.x = -1;

        elements[obj.id] = {
            id: obj.id,
            spriteName: obj.sprite,
            element,
            label,
            sprite,
            time: Date.now(),
        }
        element.addChild(sprite);
        app.stage.addChild(element);

    }
}

function endGame(body) {
    if (ended) {
        return;
    }
    ended = true;
    resetGame();
    $("#container").append($(`<div id="winner"><b>${body.winner}</b><br />WON THE IRON THRONES</div>`));
    $("#container").addClass("ended").addClass(body.winner)
}

function gameLoop(delta) {
    for (id in elements) {
        const el = elements[id];
        if (Date.now() - el.time > 5000) {
            removeGameObject(el);
        }
    }
}