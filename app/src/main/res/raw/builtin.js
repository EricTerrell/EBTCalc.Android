  var Prompt = com.ericbt.rpncalc.javascript.Prompt();

// Global methods

function javascriptArray(array) {
  var result = [];

  for (var i = 0, length = array.length; i < length; i++) {
    result.push(array[i]);
  }

  return result;
}

function sign(x) { return x >= 0 ? 1 : -1; }

function leftPad(ch, str, len) {
  while (str.length < len) {
    str = ch + str;
  }

  return str;
}

// category Main "(Main)"

function Main() {}

// button Main.absoluteValue "|x|"
Main.absoluteValue = function(x) { return Math.abs(x); };

// button Main.integerPart "Integer Part(x)"
Main.integerPart = function(x) { return sign(x) * Math.floor(Math.abs(x)); };

// button Main.fractionalPart "Fractional Part(x)"
Main.fractionalPart = function(x) { return x - Main.integerPart(x); };

// button Main.round "Round(x)"
Main.round = function(x) { return sign(x) * Math.floor(Math.abs(x) + 0.5); };

// button Main.floor "⌊x⌋"
Main.floor = function(x) { return Math.floor(x); };

// button Main.ceiling "⌈x⌉"
Main.ceiling = function(x) { return Math.ceil(x); };

Main.Log = function(x) { return Math.log(x) / Math.log(10); };

// button Main.tenToX "10^x"
Main.tenToX = function(x) { return Math.pow(10, x); };

Main.e = function() { return Math.E; };

Main.LN = function(x) { return Math.log(x); };

// button Main.e_To_x "e^x"
Main.e_To_x = function(x) { return Math.pow(Math.E, x); };

Main.modulo = function(n, divisor) {
  const remain = n % divisor;
  return Math.floor(remain >= 0 ? remain : remain + divisor);
};

// button Main.findFraction "Find Fraction(x, n)"
Main.findFraction = function(number, iterations) {
  var wholePart = Main.integerPart(number);
  var savedSign = wholePart === 0 ? sign(number) : 1;
  var fraction = number - wholePart;
  fraction = Math.abs(fraction);
  var numerator = 0;
  var denominator = 1;
  var bn = 0;
  var bd = 0;
  var error = 0;

  for (var i = 0; i < iterations; i++) {
    var tempFraction = numerator / denominator;
    var tempError = Math.abs(tempFraction - fraction);
    
    if (i === 0 || tempError < error) {
      error = tempError;
      bn = numerator;
      bd = denominator;
    }

    if (tempFraction < fraction) {
      numerator++;
    }
    else {
      denominator++;
    }
  }

  return [wholePart, savedSign * bn, bd];
};

function Trig() {}

// button Trig.radians "Degrees→Radians"
Trig.radians = function(degrees) { return degrees * Math.PI / 180; };

// button Trig.degrees "Radians→Degrees"
Trig.degrees = function(radians) { return radians * 180 / Math.PI; };

Trig.Sin = function(degrees) { return Math.sin(Trig.radians(Trig.normalizeAngle(degrees))); };

Trig.ASin = function(x) { return Trig.degrees(Math.asin(x)); };

Trig.Cos = function(degrees) { return Math.cos(Trig.radians(Trig.normalizeAngle(degrees))); };

Trig.ACos = function(x) { return Trig.degrees(Math.acos(x)); };

Trig.Tan = function(degrees) { return degrees !== 90 ? Math.tan(Trig.radians(Trig.normalizeAngle(degrees))) : NaN; };

Trig.ATan = function(x) { return Trig.degrees(Math.atan(x)); };

Trig.SinH = function(x) { return Math.sinh(x); };

Trig.ASinH = function(x) { return Math.asinh(x); };

Trig.CosH = function(x) { return Math.cosh(x); };

Trig.ACosH = function(x) { return Math.acosh(x); };

Trig.TanH = function(x) { return Math.tanh(x); };

Trig.ATanH = function(x) { return Math.atanh(x); };

Trig.normalizeAngle = function(degrees) {
    const circleDegrees = 360.0;

    const multiple = Math.abs(degrees) / circleDegrees;

    if (multiple > 1.0) {
    const sign = Math.sign(degrees);

    degrees = (Math.abs(degrees) - (circleDegrees * Math.floor(multiple))) * sign;
    }

    return degrees;
}

function Statistics() {}

Statistics.Mean = function(array) {
  return array.reduce(function(a, b) { return a + b; }) / array.length;
};

Statistics.Median = function(array) {
  array = array.sort(function(a, b) { return a - b; });

  var result;

  var index = Math.floor(array.length / 2);

  if (array.length % 2 === 1) {
    result = array[index];
  }
  else {
    result = (array[index - 1] + array[index]) / 2;
  }

  return result;
};

Statistics.Variance = function(array) {
  var mean = Statistics.Mean(array);

  var sum = 0;

  for (var i = 0, length = array.length; i < length; i++) {
    sum += Math.pow(array[i] - mean, 2);
  }

  return sum / array.length;
};

// button Statistics.standardDeviation "Std. Dev.(array)"
Statistics.standardDeviation = function(array) { return Math.sqrt(Statistics.Variance(array)); };

function Memory() {}

// button Memory.CreateVariable "Create Variable(value)"
Memory.CreateVariable = function(value) { 
  var values = Prompt.prompt(
    [ 
      ["Variable", "v", "[a-zA-Z0-9_ ]+", ""]
    ], "Create Variable");

  if (values !== null) {
    Globals[values[0]] = value;
    GlobalsModified = true;
  }
  else {
    // If the user cancelled, the value needs to go back on the stack.
    return value;
  }
};

// button Memory.retrieveVariable "Retrieve Variable"
Memory.retrieveVariable = function() { 
  var keys = Memory._keys();

  if (keys.length > 0) {
    var values = Prompt.prompt(
      [ 
        ["Variable", "s", keys, ""]
      ], "Retrieve Variable");
  
    if (values !== null) {
      return Globals[values[0]];
    }
  }
};

// button Memory.updateVariable "Update Variable(value)"
Memory.updateVariable = function(value) { 
  var values = Prompt.prompt(
    [ 
      ["Variable", "s", Memory._keys(), ""]
    ], "Select Variable");

  if (values !== null) {
    Globals[values[0]] = value;
    GlobalsModified = true;
  }
  else {
    // If the user cancelled, the value needs to go back on the stack.
    return value;
  }
};

// button Memory.deleteVariable "Delete Variable"
Memory.deleteVariable = function() { 
  var keys = Memory._keys();

  if (keys.length > 0) {
    var values = Prompt.prompt(
      [ 
        ["Variable", "s", Memory._keys(), ""]
      ], "Delete Variable");

    if (values !== null) {
      delete Globals[values[0]];
      GlobalsModified = true;
    }
  }
};

// button Memory.deleteAllVariables "Delete All Variables"
Memory.deleteAllVariables = function() { 
  Globals = {}; 
  GlobalsModified = true;
};

Memory._keys = function() {
  var keyArray = [];
  
  for (var key in Globals) {
    keyArray.push(key);
  }
  
  keyArray.sort();

  var keys = "";

  for (var i = 0, len = keyArray.length; i < len; i++) {
    keys += keyArray[i] + "|";
  }

  if (keys.length > 0) {
    keys = keys.substring(0, keys.length - 1);
  }

  return keys;
};

function Dates() {}

Dates.Now = function() { return new Date(); };

// button Dates.enterDateTime "Enter Date/Time"
Dates.enterDateTime = function() {
  var values = Prompt.prompt(
    [ 
      ["Year", "v", "[0-9]{4}", ""],
      ["Month", "s", "1|2|3|4|5|6|7|8|9|10|11|12", ""],
      ["Day", "v", "[0-9]{1,2}", ""],
      ["Time (hh:mm)", "v", "[0-9]{1,2}:[0-9]{1,2}", "00:00"],
      ["am/pm", "s", "AM|PM", "1"]
    ], "Enter Date/Time");

  if (values !== null) {
    return new Date(values[1] + "/" + values[2] + "/" + values[0] + " " + values[3] + " " + values[4]);
  }
};

// button Dates.diffInDays "Diff in Days"
Dates.diffInDays = function(d1, d2) { return (d1 - d2) / (1000 * 60 * 60 * 24); };

// From date.js http://code.google.com/p/datejs/
Date.isLeapYear = function(year) { 
  return (((year % 4 === 0) && (year % 100 !== 0)) || (year % 400 === 0)); 
};

// From date.js http://code.google.com/p/datejs/
Date.getDaysInMonth = function(year, month) {
  return [31, (Date.isLeapYear(year) ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
};

// From date.js http://code.google.com/p/datejs/
Date.prototype.isLeapYear = function() { 
  var y = this.getFullYear(); 
  return (((y % 4 === 0) && (y % 100 !== 0)) || (y % 400 === 0)); 
};

// From date.js http://code.google.com/p/datejs/
Date.prototype.getDaysInMonth = function() { 
  return Date.getDaysInMonth(this.getFullYear(), this.getMonth());
};

// From date.js http://code.google.com/p/datejs/
Date.prototype.addMonths = function(value) {
  var n = this.getDate();
  this.setDate(1);
  this.setMonth(this.getMonth() + value);
  this.setDate(Math.min(n, this.getDaysInMonth()));
  return this;
};

// category Interval "Dates" 

function Interval() {
  this.years = 0;
  this.months = 0;
  this.days = 0;
  this.hours = 0;
  this.minutes = 0;
  this.seconds = 0;
  this.milliseconds = 0;
}

Interval._init = function(years, months, days, hours, minutes, seconds, milliseconds) {
  var interval = new Interval();
  interval.years = years;
  interval.months = months;
  interval.days = days;
  interval.hours = hours;
  interval.minutes = minutes;
  interval.seconds = seconds;
  interval.milliseconds = milliseconds;

  return interval;
};

// button Dates.addInterval "Add Interval"
Dates.prototype.addInterval = function(date, interval) {
  var result = new Date(date.addMonths(interval.months));

  result.setFullYear(result.getFullYear() + interval.years);

  result = new Date(result.getTime() + 
                    interval.days * 24 * 60 * 60 * 1000 + 
                    interval.hours * 60 * 60 * 1000 + 
                    interval.minutes * 60 * 1000 + 
                    interval.seconds * 1000 + 
                    interval.milliseconds);

  return result;
};

// button Dates.subtractInterval "Subtract Interval"
Dates.prototype.subtractInterval = function(date, interval) {
  interval._negate();

  return this.addInterval(date, interval);
};

Interval.prototype.toString = function() {
  var s = "";

  if (this.years !== 0) {
    s += this.years + ' Years, ';
  }

  if (this.months !== 0) {
    s += this.months + ' Months, ';
  }

  if (this.days !== 0) {
    s += this.days + ' Days, ';
  }

  if (this.hours !== 0) {
    s += this.hours + ' Hours, ';
  }

  if (this.minutes !== 0) {
    s += this.minutes + ' Minutes, ';
  }

  if (this.seconds !== 0) {
    s += this.seconds + ' Seconds, ';
  }

  if (this.milliseconds !== 0) {
    s += this.milliseconds + ' Milliseconds, ';
  }

  if (s.length === 0) {
    s = 'Empty Interval';
  }
  else {
    s = s.substring(0, s.length - 2);
  }

  return s;
};

function toInt(s) {
  var result = 0;

  if (s.length > 0) {
    result = parseInt(s, 10);
  }

  return result;
}

// button Interval.enterInterval "Enter Interval"
Interval.prototype.enterInterval = function() {
  var ir = '[-?0-9]*';

  var values = Prompt.prompt(
    [ 
      ["Years", "v", ir, ""],
      ["Months", "v", ir, ""],
      ["Days", "v", ir, ""],
      ["Hours", "v", ir, ""],
      ["Minutes", "v", ir, ""],
      ["Seconds", "v", ir, ""],
      ["Milliseconds", "v", ir, ""]
    ], "Enter Interval");

  if (values !== null) {
    return Interval._init(toInt(values[0]), toInt(values[1]), toInt(values[2]), toInt(values[3]), toInt(values[4]), toInt(values[5]), toInt(values[6]));
  }
};

Interval.prototype._negate = function() {
  this.years *= -1;
  this.months *= -1;
  this.days *= -1;
  this.hours *= -1;
  this.minutes *= -1;
  this.seconds *= -1;
  this.milliseconds *= -1;
};

function Log() {}

Log._clear = function() { 
  Globals.log = []; 

  GlobalsModified = true;
};

Log._addLine = function(text) { 
  if (!Globals.log) {
    Globals.log = [];
  }

  Globals.log.push(text);

  var maxLines = 1000;

  while (Globals.log.length > maxLines) {
    Globals.log.splice(0, 1);
  }
  
  GlobalsModified = true;
};

Log.Clear = function() { Log._clear(); };

// button Log.getLog "Get Log"
Log.getLog = function() {
  if (!Globals.log) {
    Globals.log = [];
  }

  var result = "";

  for (var i = 0, len = Globals.log.length; i < len; i++) {
    result += Globals.log[i] + "\n";
  }

  return result;
};

function BaseNumber(base, number) {
  this.base = base;
  this.number = number;
}

BaseNumber.prototype.toString = function() {
  var baseNames = [];

  baseNames[2] = "BIN";
  baseNames[8] = "OCT";
  baseNames[10] = "DEC";
  baseNames[16] = "HEX";

  return baseNames[this.base] + " " + this.number;
};

BaseNumber.prototype.length = function() {
  return number.length;
};

BaseNumber.prototype.convertBase = function(targetBase) {
  var decimal = parseInt(this.number, this.base);
  return new BaseNumber(targetBase, decimal.toString(targetBase));
};

BaseNumber.prototype.pad = function(length) {
  while (this.number.length < length) {
    this.number = '0' + this.number;
  }
};

// category ComputerMath "Computer Math"
function ComputerMath() {}

// button ComputerMath.enterBin "Enter Bin"
ComputerMath.enterBin = function() {
  var values = Prompt.prompt([ ["Binary Number", "v", "[0-1]+", ""] ], "Enter Base 2 Number");

  if (values !== null) {
    return new BaseNumber(2, values[0]);
  }
};

// button ComputerMath.enterOct "Enter Oct"
ComputerMath.enterOct = function() {
  var values = Prompt.prompt([ ["Octal Number", "v", "[0-7]+", ""] ], "Enter Base 8 Number");

  if (values !== null) {
    return new BaseNumber(8, values[0]);
  }
};

// button ComputerMath.enterDec "Enter Dec"
ComputerMath.enterDec = function() {
  var values = Prompt.prompt([ ["Decimal Number", "v", "[0-9]+", ""] ], "Enter Base 10 Number");

  if (values !== null) {
    return new BaseNumber(10, values[0]);
  }
};

// button ComputerMath.enterHex "Enter Hex"
ComputerMath.enterHex = function() {
  var values = Prompt.prompt([ ["Hexadecimal Number", "v", "[0-9a-fA-F]+", ""] ], "Enter Base 16 Number");

  if (values !== null) {
    return new BaseNumber(16, values[0].toUpperCase());
  }
};

ComputerMath._bn = function(x) {
  return x.constructor.name !== 'BaseNumber' ? new BaseNumber(10, x) : x;
};

// button ComputerMath.toBin "→Bin"
ComputerMath.toBin = function(n) {
  return this._bn(n).convertBase(2);
};

// button ComputerMath.toOct "→Oct "
ComputerMath.toOct = function(n) {
  return this._bn(n).convertBase(8);
};

// button ComputerMath.ToDec "→Dec "
ComputerMath.ToDec = function(n) {
  return this._bn(n).convertBase(10);
};

// button ComputerMath.ToHex "→Hex"
ComputerMath.ToHex = function(n) {
  return this._bn(n).convertBase(16);
};

ComputerMath._logicalOperation = function(a, b, f) {
  a = a.convertBase(2).number;
  b = b.convertBase(2).number;

  var padLength = Math.max(a.length, b.length);

  var aBits = leftPad('0', a, padLength);
  var bBits = leftPad('0', b, padLength);

  var bits = "";

  for (var i = 0; i < padLength; i++) {
    var bit;

    if (f(aBits[i], bBits[i])) {
      bit = '1';
    }
    else {
      bit = '0';
    }

    bits += bit;
  }

  return new BaseNumber(2, bits);
};

// button ComputerMath.add "+"
ComputerMath.add = function(a, b) {
  a = this._bn(a);
  b = this._bn(b);

  var base = b.base;
  
  a = ComputerMath.toDecimal(a);
  b = ComputerMath.toDecimal(b);
  
  return new BaseNumber(10, a + b).convertBase(base);
};

// button ComputerMath.subtract "−"
ComputerMath.subtract = function(a, b) {
  a = this._bn(a);
  b = this._bn(b);

  var base = b.base;
  
  a = ComputerMath.toDecimal(a);
  b = ComputerMath.toDecimal(b);
  
  return new BaseNumber(10, a - b).convertBase(base);
};

// button ComputerMath.multiply "×"
ComputerMath.multiply = function(a, b) {
  a = this._bn(a);
  b = this._bn(b);

  var base = b.base;
  
  a = ComputerMath.toDecimal(a);
  b = ComputerMath.toDecimal(b);
  
  return new BaseNumber(10, a * b).convertBase(base);
};

// button ComputerMath.divide "÷"
ComputerMath.divide = function(a, b) {
  a = this._bn(a);
  b = this._bn(b);

  var base = b.base;
  
  a = ComputerMath.toDecimal(a);
  b = ComputerMath.toDecimal(b);
  
  return new BaseNumber(10, a / b).convertBase(base);
};

ComputerMath.And = function(a, b) {
  a = this._bn(a);
  b = this._bn(b);

  var base = b.base;
  var result = ComputerMath._logicalOperation(a, b, function(a, b) { return a === "1" && b === "1"; });

  return result.convertBase(base);
};

ComputerMath.Or = function(a, b) {
  a = this._bn(a);
  b = this._bn(b);

  var base = b.base;
  var result = ComputerMath._logicalOperation(a, b, function(a, b) { return a === "1" || b === "1"; });

  return result.convertBase(base);
};

ComputerMath.XOr = function(a, b) {
  a = this._bn(a);
  b = this._bn(b);

  var base = b.base;
  var result = ComputerMath._logicalOperation(a, b, function(a, b) { return a !== b; });

  return result.convertBase(base);
};

ComputerMath.Not = function(a) {
  a = this._bn(a);

  var originalBase = a.base;

  var bin = a.convertBase(2);

  var newNumber = "";
  
  for (var i = 0, len = bin.number.length; i < len; i++) {
    var digit = bin.number[i] === "1" ? "0" : "1";
    newNumber += digit;
  }
 
  bin.number = newNumber;

  return bin.convertBase(originalBase);
};

// button ComputerMath.toDecimal "→Double"
ComputerMath.toDecimal = function(n) {
  n = this._bn(n);

  return parseInt(n.number, n.base);
};
