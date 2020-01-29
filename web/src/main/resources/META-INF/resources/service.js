var eb = new EventBus('/eventbus/');
eb.enableReconnect(true);

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
    console.log('removeGameObject');
    removeGameObject(msg.body);
  });
};

eb.onreconnect = function() {
  console.log('onreconnect');
  resetGame();
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