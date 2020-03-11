var eb = new EventBus('/eventbus/');
eb.enableReconnect(true);

eb.onopen = function () {
  console.log('onopen')
  eb.registerHandler('display', function (err, msg) {
    if (err) {
      console.log(err);
    }
    if (msg.body) {
      for (var i in msg.body) {
        displayGameObject(msg.body[i]);
      }
    }
  });
  eb.registerHandler('game', function (err, msg) {
    if (err) {
      console.log(err);
    }
    switch (msg.body.type) {
      case "game-over":
        endGame(msg.body);
        break;
      case "new-game":
        newGame();
        break;
    }
  });
};

eb.onreconnect = function() {
  console.log('onreconnect');
  newGame();
};
