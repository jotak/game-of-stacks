var eb = new EventBus('/eventbus/');
eb.enableReconnect(true);

function displayGameObject(obj) {
  var rawElt = document.getElementById(obj.id);
  if (!rawElt) {
    $('#board').append('<div id="' + obj.id + '" style="' + obj.style + '">' + obj.text + '</div>');
  } else {
    if (obj.style) {
      rawElt.style.cssText = obj.style;
    }
    if (obj.text) {
      rawElt.innerHTML = obj.text;
    }
  }
  if (obj.x) {
    $('#' + obj.id).css('top', obj.y + 'px')
      .css('left', obj.x + 'px');
  }
}

eb.onopen = function () {
  console.log('onopen')
  eb.registerHandler('displayGameObject', function (err, msg) {
    if (err) {
        console.log(err);
    }
    displayGameObject(msg.body);
  });
  eb.registerHandler('removeGameObject', function (err, msg) {
    if (err) {
        console.log(err);
    }
    $('#' + msg.body).remove();
  });

  eb.send("init-session", "", function (err, msg) {
    msg.body.forEach(function(obj) {
      displayGameObject(obj);
    });
  });
};

eb.onreconnect = function() {
  console.log('onreconnect')
  $('#board').contents().remove();
};

function play() {
  eb.send("play", "");
}

function pause() {
  eb.send("pause", "");
}

function reset() {
  eb.send("reset", "");
}

var config = {
    type: Phaser.AUTO,
    width: 1200,
    height: 1024,
    physics: {
        default: 'arcade',
        arcade: {
            gravity: { y: 200 }
        }
    },
    scene: {
        preload: preload,
        create: create
    }
};

var game = new Phaser.Game(config);

function preload (){
    this.load.setBaseURL('http://labs.phaser.io');

    this.load.image('sky', 'assets/skies/space3.png');
    this.load.image('logo', 'assets/sprites/phaser3-logo.png');
    this.load.image('red', 'assets/particles/red.png');
}

function create (){
    this.add.image(400, 300, 'sky');

    var particles = this.add.particles('red');

    var emitter = particles.createEmitter({
        speed: 100,
        scale: { start: 1, end: 0 },
        blendMode: 'ADD'
    });

    var logo = this.physics.add.image(400, 100, 'logo');

    logo.setVelocity(100, 200);
    logo.setBounce(1, 1);
    logo.setCollideWorldBounds(true);

    emitter.startFollow(logo);
}

