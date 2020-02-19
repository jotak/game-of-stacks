var eb = new EventBus('/eventbus/');
eb.enableReconnect(true);

eb.onopen = function () {
  console.log('onopen')
  eb.registerHandler('displayGameObject', function (err, msg) {
    if (err) {
      console.log(err);
    }
    if (msg.body) {
      for (var i in msg.body) {
        displayGameObject(msg.body[i]);
      }
    }
  });
  eb.registerHandler('endGame', function (err, msg) {
    if (err) {
      console.log(err);
    }
    endGame(msg.body);
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
  resetGame();
}