$(function () {
    const api = receiptsApi();
    const ctrl = receiptsController(api);

    ctrl.getReceipts()
        .then(function (receipts) {
            console.log(receipts);
            for (var i = 0; i < receipts.length; i++) {
                var receipt = receipts[i];
                $(`<div class="receipt">${receipt.merchantName}<div><span class="receiptTag">t1</span></div></div>`)
                    .appendTo($("#receiptList"));
            }
        });

    setTimeout(function() {
        ctrl.postReceipt({
            merchant: 'Amazon',
            amount: 200
        }).then(function (id) {
            setTimeout(function () {
                console.log('added ' + id);
                ctrl.tagReceipt(id, 'test')
                    .then(function () {
                        console.log('success tagging "test" on ' + id);
                    })
            }, 5000)
        });
    }, 1000)

});

function receiptsController(api) {
    function postReceipt(receipt) {
        return api.POST("/receipts", receipt);
    }

    function getReceipts() {
        return api.GET_JSON('/receipts');
    }

    function tagReceipt(id, tagName) {
        return api.PUT('/tags/' + tagName, id);
    }

    return {
        postReceipt: postReceipt,
        getReceipts: getReceipts,
        tagReceipt: tagReceipt
    }
}

function receiptsApi() {
    function POST(url, data) {
        return new Promise(function (resolve, reject) {
            $.ajax({
                type: 'POST',
                url: url,
                contentType: 'application/json', // This is the money shot
                data: JSON.stringify(data),
                complete: resolve,
                error: reject
            });
        });
    }

    function GET_JSON(url) {
        return new Promise(function (resolve) {
            $.getJSON(url, resolve)
        });
    }

    function PUT(url, data) {
        return new Promise(function (resolve, reject) {
            $.ajax({
                type: 'PUT',
                url: url,
                contentType: 'application/json', // This is the money shot
                data: JSON.stringify(data),
                complete: resolve,
                error: reject
            });
        });
    }

    return {
        POST: POST,
        GET_JSON: GET_JSON,
        PUT: PUT
    }
}