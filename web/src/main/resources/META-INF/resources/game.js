const type = PIXI.utils.isWebGLSupported() ? "WebGL" : "canvas";
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
PIXI.loader
    .add('assets/players.json')
    .load(() => {
        players = PIXI.loader.resources["assets/players.json"].textures;
        //const sprite = new PIXI.Sprite(id["varys"]);
        //sprite.anchor.x = 1
        //sprite.scale.x = -2;
        //sprite.scale.y = 2;
        //Add the rocket to the stage
        //app.stage.addChild(sprite);

        //Render the stage   
        app.renderer.render(app.stage);
    });

function resetGame() {
    elements = {};
}

function removeGameObject(obj) {
    if (elements[obj.id]) {
        app.stage.removeChild(el.sprite);
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
            el.sprite.x = obj.x;
            el.sprite.y = obj.y;
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