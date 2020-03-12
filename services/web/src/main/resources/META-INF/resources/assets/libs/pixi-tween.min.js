/*!
 * pixi-tween - v0.7.1
 * Compiled Fri, 16 Feb 2018 10:50:34 UTC
 *
 * pixi-tween is licensed under the MIT License.
 * http://www.opensource.org/licenses/mit-license
 */

(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
	typeof define === 'function' && define.amd ? define(factory) :
	(global.__pixiTween = factory());
}(this, (function () { 'use strict';

if (typeof PIXI === 'undefined') { throw 'PixiJS is required'; }

/* eslint-disable no-mixed-operators */

/**
 * @namespace PIXI.tween.Easing
 */
var Easing = {
    /**
     * linear tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of linear easing function
     */
    linear: function linear() {
        return function linear(t) {
            return t;
        };
    },


    /**
     * inQuad tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inQuad easing function
     */
    inQuad: function inQuad() {
        return function inQuad(t) {
            return t * t;
        };
    },


    /**
     * outQuad tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of outQuad easing function
     */
    outQuad: function outQuad() {
        return function outQuad(t) {
            return t * (2 - t);
        };
    },


    /**
     * inOutQuad tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inOutQuad easing function
     */
    inOutQuad: function inOutQuad() {
        return function inOutQuad(t) {
            t *= 2;
            if (t < 1) return 0.5 * t * t;

            return -0.5 * (--t * (t - 2) - 1);
        };
    },


    /**
     * inCubic tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inCubic easing function
     */
    inCubic: function inCubic() {
        return function inCubic(t) {
            return t * t * t;
        };
    },


    /**
     * outCubic tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of outCubic easing function
     */
    outCubic: function outCubic() {
        return function outCubic(t) {
            return --t * t * t + 1;
        };
    },


    /**
     * inOutCubic tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inOutCubic easing function
     */
    inOutCubic: function inOutCubic() {
        return function inOutCubic(t) {
            t *= 2;
            if (t < 1) return 0.5 * t * t * t;
            t -= 2;

            return 0.5 * (t * t * t + 2);
        };
    },


    /**
     * inQuart tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inQuart easing function
     */
    inQuart: function inQuart() {
        return function inQuart(t) {
            return t * t * t * t;
        };
    },


    /**
     * outQuart tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of outQuart easing function
     */
    outQuart: function outQuart() {
        return function outQuart(t) {
            return 1 - --t * t * t * t;
        };
    },


    /**
     * inOutQuart tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inOutQuart easing function
     */
    inOutQuart: function inOutQuart() {
        return function inOutQuart(t) {
            t *= 2;
            if (t < 1) return 0.5 * t * t * t * t;
            t -= 2;

            return -0.5 * (t * t * t * t - 2);
        };
    },


    /**
     * inQuint tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inQuint easing function
     */
    inQuint: function inQuint() {
        return function inQuint(t) {
            return t * t * t * t * t;
        };
    },


    /**
     * outQuint tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of outQuint easing function
     */
    outQuint: function outQuint() {
        return function outQuint(t) {
            return --t * t * t * t * t + 1;
        };
    },


    /**
     * inOutQuint tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inOutQuint easing function
     */
    inOutQuint: function inOutQuint() {
        return function inOutQuint(t) {
            t *= 2;
            if (t < 1) return 0.5 * t * t * t * t * t;
            t -= 2;

            return 0.5 * (t * t * t * t * t + 2);
        };
    },


    /**
     * inSine tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inSine easing function
     */
    inSine: function inSine() {
        return function inSine(t) {
            return 1 - Math.cos(t * Math.PI / 2);
        };
    },


    /**
     * outSine tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of outSine easing function
     */
    outSine: function outSine() {
        return function outSine(t) {
            return Math.sin(t * Math.PI / 2);
        };
    },


    /**
     * inOutSine tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inOutSine easing function
     */
    inOutSine: function inOutSine() {
        return function inOutSine(t) {
            return 0.5 * (1 - Math.cos(Math.PI * t));
        };
    },


    /**
     * inExpo tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inExpo easing function
     */
    inExpo: function inExpo() {
        return function inExpo(t) {
            return t === 0 ? 0 : Math.pow(1024, t - 1);
        };
    },


    /**
     * outExpo tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of outExpo easing function
     */
    outExpo: function outExpo() {
        return function outExpo(t) {
            return t === 1 ? 1 : 1 - Math.pow(2, -10 * t);
        };
    },


    /**
     * inOutExpo tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inOutExpo easing function
     */
    inOutExpo: function inOutExpo() {
        return function inOutExpo(t) {
            if (t === 0) return 0;
            if (t === 1) return 1;
            t *= 2;
            if (t < 1) return 0.5 * Math.pow(1024, t - 1);

            return 0.5 * (-Math.pow(2, -10 * (t - 1)) + 2);
        };
    },


    /**
     * inCirc tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inCirc easing function
     */
    inCirc: function inCirc() {
        return function inCirc(t) {
            return 1 - Math.sqrt(1 - t * t);
        };
    },


    /**
     * outCirc tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of outCirc easing function
     */
    outCirc: function outCirc() {
        return function outCirc(t) {
            return Math.sqrt(1 - --t * t);
        };
    },


    /**
     * inOutCirc tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inOutCirc easing function
     */
    inOutCirc: function inOutCirc() {
        return function inOutCirc(t) {
            t *= 2;
            if (t < 1) return -0.5 * (Math.sqrt(1 - t * t) - 1);

            return 0.5 * (Math.sqrt(1 - (t - 2) * (t - 2)) + 1);
        };
    },


    /**
     * inElastic tween
     *
     * @memberof PIXI.tween.Easing
     * @param {number} [a=0.1] - a
     * @param {number} [p=0.4] - p
     * @returns {function} - New instance of inElastic easing function
     */
    inElastic: function inElastic() {
        var a = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0.1;
        var p = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 0.4;

        return function inElastic(t) {
            var s = void 0;

            if (t === 0) return 0;
            if (t === 1) return 1;
            if (!a || a < 1) {
                a = 1;s = p / 4;
            } else {
                s = p * Math.asin(1 / a) / (2 * Math.PI);
            }

            return -(a * Math.pow(2, 10 * (t - 1)) * Math.sin((t - 1 - s) * (2 * Math.PI) / p));
        };
    },


    /**
     * outElastic tween
     *
     * @memberof PIXI.tween.Easing
     * @param {number} [a=0.1] - a
     * @param {number} [p=0.4] - p
     * @returns {function} - New instance of outElastic easing function
     */
    outElastic: function outElastic() {
        var a = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0.1;
        var p = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 0.4;

        return function outElastic(t) {
            var s = void 0;

            if (t === 0) return 0;
            if (t === 1) return 1;
            if (!a || a < 1) {
                a = 1;s = p / 4;
            } else {
                s = p * Math.asin(1 / a) / (2 * Math.PI);
            }

            return a * Math.pow(2, -10 * t) * Math.sin((t - s) * (2 * Math.PI) / p) + 1;
        };
    },


    /**
     * inOutElastic tween
     *
     * @memberof PIXI.tween.Easing
     * @param {number} [a=0.1] - a
     * @param {number} [p=0.4] - p
     * @returns {function} - New instance of inOutElastic easing function
     */
    inOutElastic: function inOutElastic() {
        var a = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0.1;
        var p = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 0.4;

        return function inOutElastic(t) {
            var s = void 0;

            if (t === 0) return 0;
            if (t === 1) return 1;
            if (!a || a < 1) {
                a = 1;s = p / 4;
            } else {
                s = p * Math.asin(1 / a) / (2 * Math.PI);
            }
            t *= 2;
            if (t < 1) return -0.5 * (a * Math.pow(2, 10 * (t - 1)) * Math.sin((t - 1 - s) * (2 * Math.PI) / p));

            return a * Math.pow(2, -10 * (t - 1)) * Math.sin((t - 1 - s) * (2 * Math.PI) / p) * 0.5 + 1;
        };
    },


    /**
     * inBack tween
     *
     * @memberof PIXI.tween.Easing
     * @param {number} [v=1.70158] - v
     * @returns {function} - New instance of inBack easing function
     */
    inBack: function inBack() {
        var v = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1.70158;

        return function inBack(t) {
            var s = v;

            return t * t * ((s + 1) * t - s);
        };
    },


    /**
     * outBack tween
     *
     * @memberof PIXI.tween.Easing
     * @param {number} [v=1.70158] - v
     * @returns {function} - New instance of outBack easing function
     */
    outBack: function outBack() {
        var v = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1.70158;

        return function outBack(t) {
            var s = v;

            return --t * t * ((s + 1) * t + s) + 1;
        };
    },


    /**
     * inOutBack tween
     *
     * @memberof PIXI.tween.Easing
     * @param {number} [v=1.70158] - v
     * @returns {function} - New instance of inOutBack easing function
     */
    inOutBack: function inOutBack() {
        var v = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1.70158;

        return function inOutBack(t) {
            var s = v * 1.525;

            t *= 2;
            if (t < 1) return 0.5 * (t * t * ((s + 1) * t - s));

            return 0.5 * ((t - 2) * (t - 2) * ((s + 1) * (t - 2) + s) + 2);
        };
    },


    /**
     * inBounce tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inBounce easing function
     */
    inBounce: function inBounce() {
        return function inBounce(t) {
            return 1 - Easing.outBounce()(1 - t);
        };
    },


    /**
     * outBounce tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of outBounce easing function
     */
    outBounce: function outBounce() {
        return function outBounce(t) {
            if (t < 1 / 2.75) {
                return 7.5625 * t * t;
            } else if (t < 2 / 2.75) {
                t = t - 1.5 / 2.75;

                return 7.5625 * t * t + 0.75;
            } else if (t < 2.5 / 2.75) {
                t = t - 2.25 / 2.75;

                return 7.5625 * t * t + 0.9375;
            }
            t -= 2.625 / 2.75;

            return 7.5625 * t * t + 0.984375;
        };
    },


    /**
     * inOutBounce tween
     *
     * @memberof PIXI.tween.Easing
     * @returns {function} - New instance of inOutBounce easing function
     */
    inOutBounce: function inOutBounce() {
        return function inOutBounce(t) {
            if (t < 0.5) return Easing.inBounce()(t * 2) * 0.5;

            return Easing.outBounce()(t * 2 - 1) * 0.5 + 0.5;
        };
    }
};

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) {
  return typeof obj;
} : function (obj) {
  return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj;
};





var asyncGenerator = function () {
  function AwaitValue(value) {
    this.value = value;
  }

  function AsyncGenerator(gen) {
    var front, back;

    function send(key, arg) {
      return new Promise(function (resolve, reject) {
        var request = {
          key: key,
          arg: arg,
          resolve: resolve,
          reject: reject,
          next: null
        };

        if (back) {
          back = back.next = request;
        } else {
          front = back = request;
          resume(key, arg);
        }
      });
    }

    function resume(key, arg) {
      try {
        var result = gen[key](arg);
        var value = result.value;

        if (value instanceof AwaitValue) {
          Promise.resolve(value.value).then(function (arg) {
            resume("next", arg);
          }, function (arg) {
            resume("throw", arg);
          });
        } else {
          settle(result.done ? "return" : "normal", result.value);
        }
      } catch (err) {
        settle("throw", err);
      }
    }

    function settle(type, value) {
      switch (type) {
        case "return":
          front.resolve({
            value: value,
            done: true
          });
          break;

        case "throw":
          front.reject(value);
          break;

        default:
          front.resolve({
            value: value,
            done: false
          });
          break;
      }

      front = front.next;

      if (front) {
        resume(front.key, front.arg);
      } else {
        back = null;
      }
    }

    this._invoke = send;

    if (typeof gen.return !== "function") {
      this.return = undefined;
    }
  }

  if (typeof Symbol === "function" && Symbol.asyncIterator) {
    AsyncGenerator.prototype[Symbol.asyncIterator] = function () {
      return this;
    };
  }

  AsyncGenerator.prototype.next = function (arg) {
    return this._invoke("next", arg);
  };

  AsyncGenerator.prototype.throw = function (arg) {
    return this._invoke("throw", arg);
  };

  AsyncGenerator.prototype.return = function (arg) {
    return this._invoke("return", arg);
  };

  return {
    wrap: function (fn) {
      return function () {
        return new AsyncGenerator(fn.apply(this, arguments));
      };
    },
    await: function (value) {
      return new AwaitValue(value);
    }
  };
}();





var classCallCheck = function (instance, Constructor) {
  if (!(instance instanceof Constructor)) {
    throw new TypeError("Cannot call a class as a function");
  }
};

var createClass = function () {
  function defineProperties(target, props) {
    for (var i = 0; i < props.length; i++) {
      var descriptor = props[i];
      descriptor.enumerable = descriptor.enumerable || false;
      descriptor.configurable = true;
      if ("value" in descriptor) descriptor.writable = true;
      Object.defineProperty(target, descriptor.key, descriptor);
    }
  }

  return function (Constructor, protoProps, staticProps) {
    if (protoProps) defineProperties(Constructor.prototype, protoProps);
    if (staticProps) defineProperties(Constructor, staticProps);
    return Constructor;
  };
}();









var inherits = function (subClass, superClass) {
  if (typeof superClass !== "function" && superClass !== null) {
    throw new TypeError("Super expression must either be null or a function, not " + typeof superClass);
  }

  subClass.prototype = Object.create(superClass && superClass.prototype, {
    constructor: {
      value: subClass,
      enumerable: false,
      writable: true,
      configurable: true
    }
  });
  if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
};











var possibleConstructorReturn = function (self, call) {
  if (!self) {
    throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
  }

  return call && (typeof call === "object" || typeof call === "function") ? call : self;
};

/**
 * Fired when the tween starts. If the tween has a delay, this event fires when the delay time is ended.
 *
 * @event PIXI.tween.Tween#start
 */

/**
 * Fired when the tween is over. If the .loop option it's true, this event never will be fired.
 * If the tween has an .repeat number, this event will be fired just when all the repeats are done.
 *
 * @event PIXI.tween.Tween#end
 */

/**
 * Fired at every repeat cycle, if your tween has .repeat=5 this events will be fired 5 times.
 *
 * @event PIXI.tween.Tween#repeat
 * @param {number} repeat - Number of times this tween has repeated
 */

/**
 * Fired at each frame
 *
 * @event PIXI.tween.Tween#update
 * @param {number} progress - 0-1 decimal value representing proportion of completion.
 * @param {number} elapsedTime - How much time in ms that has passed since the tween started.
 */

/**
 * Fired only when it's used the .stop() method. It's useful to know when a timer is cancelled.
 *
 * @event PIXI.tween.Tween#stop
 */

/**
 * If the pingPong option it's true, this events will be fired when the tweens returns back.
 *
 * @event PIXI.tween.Tween#pingpong
 */

/**
 * Quickly configure a tween via an object / json
 *
 * @typedef {Object} PIXI.tween.Tween#tweenConfig
 * @property {Object} [from]
 * @property {Object} [to]
 * @property {number} [delay]
 * @property {function} [easing]
 * @property {boolean} [expire]
 * @property {boolean} [loop]
 * @property {Object} [path]
 * @property {boolean} [pathReverse]
 * @property {boolean} [pingPong]
 * @property {number} [repeat]
 * @property {number} [time]
 * @property {number} [speed]
 * @property {Object} [on]
 * @property {function} [on.end]
 * @property {function} [on.pingpong]
 * @property {function} [on.repeat]
 * @property {function} [on.start]
 * @property {function} [on.stop]
 * @property {function} [on.update]
 */

/**
 * Tween class
 *
 * @class
 * @memberof PIXI.tween
 */

var Tween = function (_PIXI$utils$EventEmit) {
    inherits(Tween, _PIXI$utils$EventEmit);

    /**
     * @param {*} target - Target object to tween
     * @param {PIXI.tween.TweenManager} [manager] - Tween manager to handle this tween
     * @param {PIXI.tween.Tween#tweenConfig} [config] - object to configure the tween
     */
    function Tween(target, manager, config) {
        classCallCheck(this, Tween);

        var _this = possibleConstructorReturn(this, (Tween.__proto__ || Object.getPrototypeOf(Tween)).call(this));

        _this.target = target;
        if (manager) {
            _this.addTo(manager);
        }

        _this.clear();
        if (config) {
            _this.config(config);
        }
        return _this;
    }

    /**
     * Clears all class data, meaning that the tween will now do nothing if start is called
     *
     * @returns {PIXI.tween.Tween} - This tween instance
     */


    createClass(Tween, [{
        key: 'clear',
        value: function clear() {
            /** @member {PIXI.tween.Easing} - Either an easing function from PIXI.tween.Easing or a custom easing */
            this.easing = Easing.linear();

            /** @member {boolean} - Set true if you want to delete this instance when the animation it's done */
            this.expire = false;

            /** @member {number} - Times to repeat this tween */
            this.repeat = 0;

            /** @member {boolean} - Set true if you want to loop this tween forever */
            this.loop = false;

            /** @member {number} - Set a delay time in milliseconds before the tween starts */
            this.delay = 0;

            /** @member {boolean} - Set true to repeat the tween from the end point back to the start point */
            this.pingPong = false;

            /** @member {PIXI.tween.TweenPath} - Set an instance of TweenPath to animate an object along the path */
            this.path = null;

            /** @member {boolean} - Set true to reverse the direction along the path */
            this.pathReverse = false;

            /** @member {number} - How long to animate this tween over */
            this.time = 0;

            /** @member {number} - The speed that the tween will play at. 0 effectively pauses it, 1 is normal speed */
            this.speed = 1;

            this._active = false;
            this._isStarted = false;
            this._isEnded = false;

            this._to = {};
            this._from = {};
            this._resetFromOnStart = false;
            this._delayTime = 0;
            this._elapsedTime = 0;
            this._progress = 0;
            this._repeat = 0;
            this._pingPong = false;

            this._pathFrom = 0;
            this._pathTo = 0;

            this._chainTween = null;
            this._resolvePromise = null;

            return this;
        }

        /**
         * Configures the tween via a config object
         *
         * @param {PIXI.tween.Tween#tweenConfig} config - object to configure the tween
         * @returns {PIXI.tween.Tween} - This tween instance
         */

    }, {
        key: 'config',
        value: function config(_config) {
            if (!_config || (typeof _config === 'undefined' ? 'undefined' : _typeof(_config)) !== 'object') {
                return this;
            }

            if (_typeof(_config.from) === 'object') {
                this.from(_config.from);
            }
            if (_config.to && _typeof(_config.to) === 'object') {
                this.to(_config.to);
            }
            if (typeof _config.delay === 'number') {
                this.delay = _config.delay;
            }
            if (_config.easing) {
                if (typeof _config.easing === 'string' && Easing[_config.easing]) {
                    this.easing = Easing[_config.easing]();
                } else if (typeof _config.easing === 'function') {
                    this.easing = _config.easing;
                }
            }
            if (typeof _config.expire === 'boolean') {
                this.expire = _config.expire;
            }
            if (typeof _config.loop === 'boolean') {
                this.loop = _config.loop;
            }
            if (_typeof(_config.path) === 'object') {
                this.path = _config.path;
            }
            if (typeof _config.pathReverse === 'boolean') {
                this.pathReverse = _config.pathReverse;
            }
            if (typeof _config.pingPong === 'boolean') {
                this.pingPong = _config.pingPong;
            }
            if (typeof _config.repeat === 'number') {
                this.repeat = _config.repeat;
            }
            if (typeof _config.time === 'number') {
                this.time = _config.time;
            }
            if (typeof _config.speed === 'number') {
                this.speed = _config.speed;
            }

            if (_config.on && _typeof(_config.on) === 'object') {
                if (typeof _config.on.end === 'function') {
                    this.on('end', _config.on.end);
                }
                if (typeof _config.on.pingpong === 'function') {
                    this.on('pingpong', _config.on.pingpong);
                }
                if (typeof _config.on.repeat === 'function') {
                    this.on('repeat', _config.on.repeat);
                }
                if (typeof _config.on.start === 'function') {
                    this.on('start', _config.on.start);
                }
                if (typeof _config.on.stop === 'function') {
                    this.on('stop', _config.on.stop);
                }
                if (typeof _config.on.update === 'function') {
                    this.on('update', _config.on.update);
                }
            }

            return this;
        }

        /**
         * True if the tween is running
         *
         * @member {boolean}
         * @readonly
         */

    }, {
        key: 'addTo',


        /**
         * Add the tween to a manager
         *
         * @param {PIXI.tween.TweenManager} manager - Tween manager to handle this tween
         * @returns {PIXI.tween.Tween} - This tween instance
         */
        value: function addTo(manager) {
            this.manager = manager;
            this.manager.addTween(this);

            return this;
        }

        /**
         * Remove the tween from the manager if it has one
         *
         * @returns {PIXI.tween.Tween} - This tween instance
         */

    }, {
        key: 'remove',
        value: function remove() {
            if (!this.manager) {
                return this;
            }

            this.manager.removeTween(this);

            return this;
        }

        /**
         * Starts the tween
         *
         * @param {Promise} resolve - Promise to resolve when the tween has ended
         * @returns {PIXI.tween.Tween} - This tween instance
         */

    }, {
        key: 'start',
        value: function start(resolve) {
            this._active = true;
            this._isStarted = false;

            if (this._resetFromOnStart) {
                this._from = {};
            }

            if (!this._resolvePromise && resolve) {
                this._resolvePromise = resolve;
            }

            return this;
        }

        /**
         * Starts the tween, whilst returning a new promise
         *
         * @returns {Promise} - Promsie that will resolve when the tween has finished
         */

    }, {
        key: 'startPromise',
        value: function startPromise() {
            var _this2 = this;

            if (!Promise) {
                return this.start();
            }

            if (this._resolvePromise) {
                return Promise.resolve();
            }

            return new Promise(function (resolve) {
                _this2.start(resolve);
            });
        }

        /**
         * Stop the tweens progress
         *
         * @fires PIXI.tween.Tween#stop
         *
         * @param {boolean} [end=false] - Force end to be called
         * @returns {PIXI.tween.Tween} - This tween instance
         */

    }, {
        key: 'stop',
        value: function stop() {
            var end = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;

            this._active = false;
            this.emit('stop');

            if (end) {
                this._end();
            }

            return this;
        }

        /**
         * Set the end data for the tween
         *
         * @example
         * tween.to({ x:100, y:100 });
         *
         * @param {Object} data - Object containing end point data for the tween
         * @returns {PIXI.tween.Tween} - This tween instance
         */

    }, {
        key: 'to',
        value: function to() {
            var data = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

            this._to = data;

            return this;
        }

        /**
         * Set the start point data for the tween.
         * If nothing is set, data is reset so that starting the tween will use the objects current state as the start point
         *
         * @example
         * tween.from({ x:50, y:50 });
         *
         * @param {Object} [data] - Object containing start point data for the tween
         * @returns {PIXI.tween.Tween} - This tween instance
         */

    }, {
        key: 'from',
        value: function from(data) {
            if (!data || (typeof data === 'undefined' ? 'undefined' : _typeof(data)) !== 'object') {
                this._resetFromOnStart = true;
                this._from = {};
            } else {
                this._resetFromOnStart = false;
                this._from = data;
            }

            return this;
        }

        /**
         * Chain another tween to play after this tween has ended
         *
         * @param {PIXI.tween.Tween} tween - Tween to chain
         * @returns {PIXI.tween.Tween} - This tween instance
         */

    }, {
        key: 'chain',
        value: function chain(tween) {
            if (!tween) {
                tween = new Tween(this.target);
            }
            this._chainTween = tween;

            return tween;
        }

        /**
         * Resets the tween to it's default state, but keeping any to and from data, so start can be called to replay the tween
         *
         * @returns {PIXI.tween.Tween} - This tween instance
         */

    }, {
        key: 'reset',
        value: function reset() {
            this._elapsedTime = 0;
            this._progress = 0;
            this._repeat = 0;
            this._delayTime = 0;
            this._isStarted = false;
            this._isEnded = false;

            if (this.pingPong && this._pingPong) {
                var _to = this._to;
                var _from = this._from;

                this._to = _from;
                this._from = _to;

                this._pingPong = false;
            }

            return this;
        }

        /**
         * Updating of the tween; usually automatically called by its manager
         *
         * @fires PIXI.tween.Tween#start
         * @fires PIXI.tween.Tween#update
         * @fires PIXI.tween.Tween#pingpong
         * @fires PIXI.tween.Tween#repeat
         *
         * @param {number} deltaMS - Time elapsed in milliseconds from last update to this update.
         */

    }, {
        key: 'update',
        value: function update(deltaMS) {
            if (!this._canUpdate() && (this._to || this.path)) {
                return;
            }

            deltaMS *= this.speed;

            if (this.delay > this._delayTime) {
                this._delayTime += deltaMS;

                return;
            }

            if (!this._isStarted) {
                this._parseData();
                this._isStarted = true;
                this._isEnded = false;
                this.emit('start');
            }

            var time = this.pingPong ? this.time / 2 : this.time;
            var _to = void 0;
            var _from = void 0;

            if (time >= this._elapsedTime) {
                var t = this._elapsedTime + deltaMS;
                var ended = t >= time;

                this._elapsedTime = ended ? time : t;
                this._apply(time);

                var realElapsed = this._pingPong ? time + this._elapsedTime : this._elapsedTime;

                this._progress = Math.min(realElapsed / this.time, 1);

                this.emit('update', this._progress, realElapsed);

                if (ended) {
                    if (this.pingPong && !this._pingPong) {
                        this._pingPong = true;
                        _to = this._to;
                        _from = this._from;
                        this._from = _to;
                        this._to = _from;

                        if (this.path) {
                            _to = this._pathTo;
                            _from = this._pathFrom;
                            this._pathTo = _from;
                            this._pathFrom = _to;
                        }

                        this.emit('pingpong');
                        this._elapsedTime = 0;
                        this._progress = 0.5;

                        return;
                    }

                    if (this.loop || this.repeat > this._repeat) {
                        ++this._repeat;
                        this.emit('repeat', this._repeat);
                        this._elapsedTime = 0;
                        this._progress = 0;

                        if (this.pingPong && this._pingPong) {
                            _to = this._to;
                            _from = this._from;
                            this._to = _from;
                            this._from = _to;

                            if (this.path) {
                                _to = this._pathTo;
                                _from = this._pathFrom;
                                this._pathTo = _from;
                                this._pathFrom = _to;
                            }

                            this._pingPong = false;
                        }

                        return;
                    }

                    this._end();
                }

                return;
            }
        }

        /**
         * Called when the tween has finished
         *
         * @fires PIXI.tween.Tween#end
         *
         * @private
         */

    }, {
        key: '_end',
        value: function _end() {
            this._isEnded = true;
            this._active = false;
            this.emit('end');
            this._elapsedTime = 0;

            if (this._chainTween) {
                if (!this._chainTween.manager) {
                    this._chainTween.addTo(this.manager);
                }
                this._chainTween.start(this._resolvePromise);
                this._resolvePromise = null;
            } else if (this._resolvePromise) {
                var resolvePromise = this._resolvePromise;

                this._resolvePromise = null;
                resolvePromise();
            }
        }

        /**
         * Parses the from and to data to extract details about how the tween should progress
         *
         * @private
         */

    }, {
        key: '_parseData',
        value: function _parseData() {
            if (this._isStarted) {
                return;
            }

            _parseRecursiveData(this._to, this._from, this.target);

            if (this.path) {
                var distance = this.path.totalDistance();

                if (this.pathReverse) {
                    this._pathFrom = distance;
                    this._pathTo = 0;
                } else {
                    this._pathFrom = 0;
                    this._pathTo = distance;
                }
            }
        }

        /**
         * Updates the object with the tween settings
         *
         * @param {number} time - Time duration for the tween
         * @private
         */

    }, {
        key: '_apply',
        value: function _apply(time) {
            _recursiveApplyTween(this._to, this._from, this.target, time, this._elapsedTime, this.easing);

            if (this.path) {
                var _time = this.pingPong ? this.time / 2 : this.time;
                var b = this._pathFrom;
                var c = this._pathTo - this._pathFrom;
                var d = _time;
                var t = _time ? this._elapsedTime / d : 1;

                var distance = b + c * this.easing(t);
                var pos = this.path.getPointAtDistance(distance);

                this.target.position.set(pos.x, pos.y);
            }
        }

        /**
         * Can this tween be updated (must be active and have a target destination)
         *
         * @returns {boolean} - True if this tween can be updated
         * @private
         */

    }, {
        key: '_canUpdate',
        value: function _canUpdate() {
            return this._active && this.target;
        }
    }, {
        key: 'active',
        get: function get$$1() {
            return this._active;
        }

        /**
         * How much time has passed on an active tween
         *
         * @member {number}
         * @readonly
         */

    }, {
        key: 'elapsedTime',
        get: function get$$1() {
            return this._elapsedTime;
        }

        /**
         * 0-1 decimal value representing proportion of completion
         *
         * @member {number}
         * @readonly
         */

    }, {
        key: 'progress',
        get: function get$$1() {
            return this._progress;
        }

        /**
         * True if the tween has started running
         *
         * @member {boolean}
         * @readonly
         */

    }, {
        key: 'isStarted',
        get: function get$$1() {
            return this._isStarted;
        }

        /**
         * True if a tween has ended running
         *
         * @member {boolean}
         * @readonly
         */

    }, {
        key: 'isEnded',
        get: function get$$1() {
            return this._isEnded;
        }
    }]);
    return Tween;
}(PIXI.utils.EventEmitter);

function _recursiveApplyTween(to, from, target, time, elapsedTime, easing) {
    for (var k in to) {
        if (!_isObject(to[k])) {
            var b = from[k];
            var c = to[k] - from[k];
            var d = time;
            var t = time ? elapsedTime / d : 1;

            target[k] = b + c * easing(t);
        } else {
            _recursiveApplyTween(to[k], from[k], target[k], time, elapsedTime, easing);
        }
    }
}

function _parseRecursiveData(to, from, target) {
    for (var k in to) {
        if (from[k] !== 0 && !from[k]) {
            if (_isObject(target[k])) {
                from[k] = {};
                _parseRecursiveData(to[k], from[k], target[k]);
            } else {
                from[k] = target[k];
            }
        }
    }
}

function _isObject(obj) {
    return Object.prototype.toString.call(obj) === '[object Object]';
}

/**
 * Tween manager class, that handles updating child Tweens
 *
 * @class
 * @memberof PIXI.tween
 */

var TweenManager = function () {
    /** */
    function TweenManager() {
        classCallCheck(this, TweenManager);

        /** @member {Array.<PIXI.tween.Tween>} - The array of tweens being manager */
        this.tweens = [];

        this._tweensToDelete = [];
        this._last = 0;
    }

    /**
     * Updating of the tween manager, which will in turn update any active tweens
     *
     * @param {number} [deltaMS] - If not provided, an internal deltaMS will be calculated
     */


    createClass(TweenManager, [{
        key: 'update',
        value: function update(deltaMS) {
            if (!deltaMS && deltaMS !== 0) {
                deltaMS = this._getDeltaMS();
            }

            for (var i = 0; i < this.tweens.length; ++i) {
                var tween = this.tweens[i];

                if (tween.active) {
                    tween.update(deltaMS);
                }

                if (tween.isEnded && tween.expire) {
                    tween.remove();
                }
            }

            if (this._tweensToDelete.length) {
                for (var _i = 0; _i < this._tweensToDelete.length; ++_i) {
                    this._remove(this._tweensToDelete[_i]);
                }
                this._tweensToDelete.length = 0;
            }
        }

        /**
         * Returns an array with all the tweens for the given target.
         *
         * @param {any} target - The target object to check for tweens
         * @returns {Array.<PIXI.tween.Tween>} - Tweens attached to the given target
         */

    }, {
        key: 'getTweensForTarget',
        value: function getTweensForTarget(target) {
            var tweens = [];

            for (var i = 0; i < this.tweens.length; ++i) {
                if (this.tweens[i].target === target) {
                    tweens.push(this.tweens[i]);
                }
            }

            return tweens;
        }

        /**
         * Returns a new tween instance that is managed by this tween manager.
         *
         * @param {any} target - The target object to add a tween to
         * @param {PIXI.tween.Tween#tweenConfig} [config] - object to configure the tween
         * @returns {PIXI.tween.Tween} - New tween instance
         */

    }, {
        key: 'createTween',
        value: function createTween(target, config) {
            return new Tween(target, this, config);
        }

        /**
         * Normally you want to use .createTween(target) to create a tween, but, you can also create a tween
         * with new PIXI.Tween(target) and add it in the manager with this method.
         *
         * @param {PIXI.tween.Tween} tween - Tween to add
         */

    }, {
        key: 'addTween',
        value: function addTween(tween) {
            if (tween) {
                tween.manager = this;
                this.tweens.push(tween);
            }
        }

        /**
         * Removes a tween from being managed by this instance
         *
         * @param {PIXI.tween.Tween} tween - Tween to remove
         */

    }, {
        key: 'removeTween',
        value: function removeTween(tween) {
            if (tween) {
                this._tweensToDelete.push(tween);
            }
        }

        /**
         * Delets the tween from the array of managed tweens
         *
         * @param {PIXI.tween.Tween} tween - Tween to remove
         * @private
         */

    }, {
        key: '_remove',
        value: function _remove(tween) {
            var index = this.tweens.indexOf(tween);

            if (index !== -1) {
                this.tweens.splice(index, 1);
            }
        }

        /**
         * Returns a new tween instance that is managed by this tween manager.
         *
         * @returns {number} - How much time in ms has passed since update was last called
         * @private
         */

    }, {
        key: '_getDeltaMS',
        value: function _getDeltaMS() {
            if (this._last === 0) {
                this._last = Date.now();
            }
            var now = Date.now();
            var deltaMS = now - this._last;

            this._last = now;

            return deltaMS;
        }
    }]);
    return TweenManager;
}();

/**
 * Class that allows drawing a path using PIXI.Graphics, which can then be followed and used as a tween
 *
 * @class
 * @memberof PIXI.tween
 */
var TweenPath = function () {
    /** */
    function TweenPath() {
        classCallCheck(this, TweenPath);

        /** @member {PIXI.Polygon} - PIXI object to use as the path */
        this.polygon = new PIXI.Polygon();
        this.polygon.closed = false;

        /** @member {PIXI.GraphicsData} - Current path */
        this.currentPath = null;

        /** @member {Array.<PIXI.GraphicsData>} - Graphics data */
        this.graphicsData = [];

        /**
         * @member {boolean}
         * Used to detect if the graphics object has changed.
         * If this is set to true then the graphics object will be recalculated.
         */
        this.dirty = true;

        this._closed = false;
        this._tmpPoint = new PIXI.Point();
        this._tmpPoint2 = new PIXI.Point();
        this._tmpDistance = [];
    }

    /**
     * Set true to close your path
     *
     * @member {boolean}
     */


    createClass(TweenPath, [{
        key: "moveTo",


        /**
         * Clear the path
         * Moves the current drawing position to x, y.
         * @see https://pixijs.github.io/docs/PIXI.Graphics.html#moveTo
         *
         * @param {number} x - the X coordinate to move to
         * @param {number} y - the Y coordinate to move to
         * @returns {PIXI.tween.TweenPath} - This instance of TweenPath
         */
        value: function moveTo(x, y) {
            PIXI.Graphics.prototype.moveTo.call(this, x, y);
            this.dirty = true;

            return this;
        }

        /**
         * Draws a line using the current line style from the current drawing position to (x, y);
         * The current drawing position is then set to (x, y).
         * @see https://pixijs.github.io/docs/PIXI.Graphics.html#lineTo
         *
         * @param {number} x - the X coordinate to draw to
         * @param {number} y - the Y coordinate to draw to     *
         * @returns {PIXI.tween.TweenPath} - This instance of TweenPath
         */

    }, {
        key: "lineTo",
        value: function lineTo(x, y) {
            PIXI.Graphics.prototype.lineTo.call(this, x, y);
            this.dirty = true;

            return this;
        }

        /**
         * Calculate the points for a quadratic bezier curve and then draws it.
         * Based on: https://stackoverflow.com/questions/785097/how-do-i-implement-a-bezier-curve-in-c
         * @see https://pixijs.github.io/docs/PIXI.Graphics.html#quadraticCurveTo
         *
         * @param {number} cpX - Control point x
         * @param {number} cpY - Control point y
         * @param {number} toX - Destination point x
         * @param {number} toY - Destination point y
         * @returns {PIXI.tween.TweenPath} - This instance of TweenPath
         */

    }, {
        key: "quadraticCurveTo",
        value: function quadraticCurveTo(cpX, cpY, toX, toY) {
            PIXI.Graphics.prototype.quadraticCurveTo.call(this, cpX, cpY, toX, toY);
            this.dirty = true;

            return this;
        }

        /**
         * Calculate the points for a bezier curve and then draws it.
         * @see https://pixijs.github.io/docs/PIXI.Graphics.html#bezierCurveTo
         *
         * @param {number} cpX - Control point x
         * @param {number} cpY - Control point y
         * @param {number} cpX2 - Second Control point x
         * @param {number} cpY2 - Second Control point y
         * @param {number} toX - Destination point x
         * @param {number} toY - Destination point y     *
         * @returns {PIXI.tween.TweenPath} - This instance of TweenPath
         */

    }, {
        key: "bezierCurveTo",
        value: function bezierCurveTo(cpX, cpY, cpX2, cpY2, toX, toY) {
            PIXI.Graphics.prototype.bezierCurveTo.call(this, cpX, cpY, cpX2, cpY2, toX, toY);
            this.dirty = true;

            return this;
        }

        /**
         * The arcTo() method creates an arc/curve between two tangents on the canvas.
         * "borrowed" from https://code.google.com/p/fxcanvas/ - thanks google!
         * @see https://pixijs.github.io/docs/PIXI.Graphics.html#arcTo
         *
         * @param {number} x1 - The x-coordinate of the beginning of the arc
         * @param {number} y1 - The y-coordinate of the beginning of the arc
         * @param {number} x2 - The x-coordinate of the end of the arc
         * @param {number} y2 - The y-coordinate of the end of the arc
         * @param {number} radius - The radius of the arc
         * @returns {PIXI.tween.TweenPath} - This instance of TweenPath
         */

    }, {
        key: "arcTo",
        value: function arcTo(x1, y1, x2, y2, radius) {
            PIXI.Graphics.prototype.arcTo.call(this, x1, y1, x2, y2, radius);
            this.dirty = true;

            return this;
        }

        /**
         * The arc method creates an arc/curve (used to create circles, or parts of circles).
         * @see https://pixijs.github.io/docs/PIXI.Graphics.html#arc
         *
         * @param {number} cx - The x-coordinate of the center of the circle
         * @param {number} cy - The y-coordinate of the center of the circle
         * @param {number} radius - The radius of the circle
         * @param {number} startAngle - The starting angle, in radians (0 is at the 3 o'clock position
         *  of the arc's circle)
         * @param {number} endAngle - The ending angle, in radians
         * @param {boolean} [anticlockwise=false] - Specifies whether the drawing should be
         *  counter-clockwise or clockwise. False is default, and indicates clockwise, while true
         *  indicates counter-clockwise.
         * @returns {PIXI.tween.TweenPath} - This instance of TweenPath
         */

    }, {
        key: "arc",
        value: function arc(cx, cy, radius, startAngle, endAngle, anticlockwise) {
            PIXI.Graphics.prototype.arc.call(this, cx, cy, radius, startAngle, endAngle, anticlockwise);
            this.dirty = true;

            return this;
        }

        /**
         * Draws the given shape to this TweenPath object. Can be any of Circle, Rectangle, Ellipse, Line or Polygon.
         *
         * @param {PIXI.Circle|PIXI.Ellipse|PIXI.Polygon|PIXI.Rectangle|PIXI.RoundedRectangle} shape - The shape object to draw.
         * @returns {PIXI.tween.TweenPath} - This instance of TweenPath
         */

    }, {
        key: "drawShape",
        value: function drawShape(shape) {
            PIXI.Graphics.prototype.drawShape.call(this, shape);
            this.dirty = true;

            return this;
        }

        /**
         * Clear the path
         *
         * @returns {PIXI.tween.TweenPath} - This instance of TweenPath
         */

    }, {
        key: "clear",
        value: function clear() {
            this.graphicsData.length = 0;
            this.currentPath = null;
            this.polygon.points.length = 0;
            this._closed = false;
            this.dirty = false;

            return this;
        }

        /**
         * Parse the list of points from the path
         *
         * @returns {PIXI.tween.TweenPath} - This instance of TweenPath
         */

    }, {
        key: "parsePoints",
        value: function parsePoints() {
            if (!this.dirty) {
                return this;
            }
            this.dirty = false;
            this.polygon.points.length = 0;
            for (var i = 0; i < this.graphicsData.length; ++i) {
                var shape = this.graphicsData[i].shape;

                if (shape && shape.points) {
                    this.polygon.points = this.polygon.points.concat(shape.points);
                }
            }

            return this;
        }

        /**
         * Finds the nth point along the path
         *
         * @param {PIXI.Point} num - Point
         * @returns {PIXI.Point} - Point co-ordinates
         */

    }, {
        key: "getPoint",
        value: function getPoint(num) {
            this.parsePoints();
            var len = this.closed && num >= this.length - 1 ? 0 : num * 2;

            this._tmpPoint.set(this.polygon.points[len], this.polygon.points[len + 1]);

            return this._tmpPoint;
        }

        /**
         * Finds the nth point along the path
         *
         * @param {PIXI.Point} num - Point
         * @returns {PIXI.Point} - Point co-ordinates
         */

    }, {
        key: "getPointAt",
        value: function getPointAt(num) {
            this.parsePoints();
            if (num > this.length) {
                return this.getPoint(this.length - 1);
            }

            if (num % 1 === 0) {
                return this.getPoint(num);
            }
            this._tmpPoint2.set(0, 0);
            var diff = num % 1;

            var _getPoint = this.getPoint(Math.ceil(num)),
                ceilX = _getPoint.x,
                ceilY = _getPoint.y;

            var _getPoint2 = this.getPoint(Math.floor(num)),
                floorX = _getPoint2.x,
                floorY = _getPoint2.y;

            var xx = -((floorX - ceilX) * diff);
            var yy = -((floorY - ceilY) * diff);

            this._tmpPoint2.set(floorX + xx, floorY + yy);

            return this._tmpPoint2;
        }

        /**
         * Finds the distance between two points
         *
         * @param {PIXI.Point} num1 - First point
         * @param {PIXI.Point} num2 - Second point
         * @returns {number} - Distance in pixels
         */

    }, {
        key: "distanceBetween",
        value: function distanceBetween(num1, num2) {
            this.parsePoints();

            var _getPoint3 = this.getPoint(num1),
                p1X = _getPoint3.x,
                p1Y = _getPoint3.y;

            var _getPoint4 = this.getPoint(num2),
                p2X = _getPoint4.x,
                p2Y = _getPoint4.y;

            var dx = p2X - p1X;
            var dy = p2Y - p1Y;

            return Math.sqrt(dx * dx + dy * dy);
        }

        /**
         * Calculates the total distance along the entire path
         *
         * @returns {number} - Distance in pixels
         */

    }, {
        key: "totalDistance",
        value: function totalDistance() {
            this.parsePoints();
            this._tmpDistance.length = 0;
            this._tmpDistance.push(0);

            var len = this.length;
            var distance = 0;

            for (var i = 0; i < len - 1; ++i) {
                distance += this.distanceBetween(i, i + 1);
                this._tmpDistance.push(distance);
            }

            return distance;
        }

        /**
         * Finds the nearest point for the distance to be travelled
         *
         * @param {number} distance - how far to travel
         * @returns {PIXI.Point} - Point co-ordinates
         */

    }, {
        key: "getPointAtDistance",
        value: function getPointAtDistance(distance) {
            this.parsePoints();
            if (!this._tmpDistance) {
                this.totalDistance();
            }
            var len = this._tmpDistance.length;
            var n = 0;

            var totalDistance = this._tmpDistance[this._tmpDistance.length - 1];

            if (distance < 0) {
                distance = totalDistance + distance;
            } else if (distance > totalDistance) {
                distance = distance - totalDistance;
            }

            for (var i = 0; i < len; ++i) {
                if (distance >= this._tmpDistance[i]) {
                    n = i;
                }

                if (distance < this._tmpDistance[i]) break;
            }

            if (n === this.length - 1) {
                return this.getPointAt(n);
            }

            var diff1 = distance - this._tmpDistance[n];
            var diff2 = this._tmpDistance[n + 1] - this._tmpDistance[n];

            return this.getPointAt(n + diff1 / diff2);
        }
    }, {
        key: "closed",
        get: function get$$1() {
            return this._closed;
        },
        set: function set$$1(value) {
            // eslint-disable-line require-jsdoc
            if (this._closed === value) {
                return;
            }
            this.polygon.closed = value;
            this._closed = value;
            this.dirty = true;
        }

        /**
         * The number of points along the path
         *
         * @member {number}
         * @readonly
         */

    }, {
        key: "length",
        get: function get$$1() {
            return this.polygon.points.length ? this.polygon.points.length / 2 + (this._closed ? 1 : 0) : 0;
        }
    }]);
    return TweenPath;
}();

// extend pixi graphics to draw tweenPaths
PIXI.Graphics.prototype.drawPath = function drawPath(path) {
    path.parsePoints();
    this.drawShape(path.polygon);

    return this;
};

/**
 * @namespace PIXI.tween
 */
var tween = {
    TweenManager: TweenManager,
    Tween: Tween,
    Easing: Easing,
    TweenPath: TweenPath
};

if (!PIXI.tweenManager) {
    PIXI.tweenManager = new TweenManager();

    PIXI.tween = tween;
}

return tween;

})));
//# sourceMappingURL=pixi-tween.js.map
