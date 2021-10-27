var operator = "=";

// 将存储到cookies中的数据拿出来
// 假设我们存储数据的格式为 username=11111.传入要获取的值的key，即可获取到数据
function getCookiesValue(key) {
    var cookie = document.cookie;
    var strings = cookie.split("; ");
    for (var i = 0; i < strings.length; i++) {
        var k = strings[i].split(operator)[0];
        var v = strings[i].split(operator)[1];
        if (k === key) {
            return v;
        }
    }
}

// 将数据存储到cookies中
function setCookiesValue(key, value) {
    document.cookie = key + operator + value;
}