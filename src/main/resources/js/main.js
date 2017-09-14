$(function () {
    const api = receiptsApi();
    const ctrl = receiptsController(api);
    const reducer = receiptsReducer();
    const elements = getElements();
    const elGen = getElementsGenerator(tagReceipt);
    const digest = getDigestFunction(elements, elGen);

    var state = reducer({}, {type: 'INIT_STATE'}),
        errorTimeout;

    function saveNewReceipt() {
        const receipt = {
            amount: elements.amountInput.val() || 0,
            merchant: elements.merchantInput.val()
        };

        if (!receipt.merchant) {
            return showInputError('Merchant required');
        }

        ctrl.postReceipt(receipt)
            .then(function (receiptId) {
                const newReceipt = {
                    id: receiptId,
                    merchantName: receipt.merchant,
                    value: receipt.amount,
                    created: (new Date()).toTimeString().split(' ')[0],
                    tags: []
                };

                state = reducer(state, {type: 'LOAD_RECEIPTS', payload: newReceipt});
                digest(state);
                cleanNewReceipt();
            })
            .catch(function (error) {
                showInputError(error.statusText);
            });
    }

    function cleanNewReceipt() {
        elements.amountInput.val('');
        elements.merchantInput.val('');
    }

    function showInputError(error) {
        elements.inputErrorElement.html(error);
        elements.inputErrorElement.fadeIn();
        clearTimeout(errorTimeout);
        errorTimeout = setTimeout(function () {
            elements.inputErrorElement.fadeOut();
        }, 5000);
    }

    function tagReceipt(receiptId, tagName) {
        return ctrl.tagReceipt(receiptId, tagName)
            .then(function () {
                reducer(state, {
                    type: 'TAG_RECEIPT', payload: {
                        id: receiptId,
                        tagName: tagName
                    }
                });
                digest(state);
            })
    }

    connectElementsBehaviours(elements, {
        saveNewReceipt: saveNewReceipt,
        cleanNewReceipt: cleanNewReceipt
    });


    /** Load the receipts initially */
    ctrl.getReceipts()
        .then(function (receipts) {
            state = reducer(state, {type: 'LOAD_RECEIPTS', payload: receipts});
            digest(state);
        });


});

function connectElementsBehaviours(elements, behaviours) {
    elements.addReceiptButton.click(function () {
        if (elements.createReceiptContainer.is(':visible')) {
            behaviours.cleanNewReceipt();
        }
        elements.createReceiptContainer.slideToggle();
    });

    elements.cancelReceiptButton.click(function () {
        elements.createReceiptContainer.slideUp();
        behaviours.cleanNewReceipt();

    });

    elements.saveReceiptButton.click(behaviours.saveNewReceipt);
}

function getElements() {
    return {
        addReceiptButton: $('#add-receipt'),
        createReceiptContainer: $('#new-receipt-container'),
        cancelReceiptButton: $('#cancel-receipt'),
        saveReceiptButton: $('#save-receipt'),
        merchantInput: $('#merchant'),
        amountInput: $('#amount'),
        receiptList: $('#receiptList'),
        inputErrorElement: $('#input-error')
    };
}

function getElementsGenerator(tagReceipt) {
    function addTagInput(receipt) {
        const tag = $(`
            <input class="tag-input" type="text" style="display: none;">
        `);

        tag.keypress(function (event) {
            var self = this;
            if (event.which === 13) {
                tagReceipt(receipt.id, this.value)
                    .then(function () {
                        self.remove();
                    });
            }
        });

        return tag;
    }

    function tagSpan(receipt, tagName) {
        const tagSpanElement = $(
            `<span class="tagValue tag-chip">
                ${tagName} x
            </span>
        `);

        tagSpanElement.click(function () {
            var self = this;

            tagReceipt(receipt.id, tagName)
                .then(function () {
                    self.remove();
                })
        });

        return tagSpanElement;
    }

    function addTagInputToggle(addTagInputElement) {
        const addTagInputElementToggle = $(`
            <button class="add-tag tag-chip">add +</button>
        `);

        addTagInputElementToggle.click(function () {
            this.remove();
            addTagInputElement.show();
            addTagInputElement.focus();
        });

        return addTagInputElementToggle;
    }

    function receiptRow(receipt) {
        const row = $(`
            <div class="receipt">
                <span>${receipt.created}</span>
                <span class="merchant">${receipt.merchantName}</span>
                <span class="amount">${receipt.value}</span>
                <span class="receiptTag tags"></span>
            </div>
        `);

        const tagColumn = row.find('.receiptTag');
        const addTagInputElement = addTagInput(receipt);
        const addTagInputElementToggle = addTagInputToggle(addTagInputElement);

        receipt.tags.forEach(function (tagName) {
            const tagElement = tagSpan(receipt, tagName);
            tagColumn.append(tagElement)
        });

        tagColumn.append(addTagInputElement);
        tagColumn.append(addTagInputElementToggle)


        return row;
    }

    return {
        receiptRow: receiptRow
    };
}

function getDigestFunction(elements, elementsGenerator) {
    return function (state) {
        elements.receiptList.empty();
        state.receipts.forEach(function (receipt) {
            elements.receiptList.append(elementsGenerator.receiptRow(receipt));
        });
    }
}

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
                contentType: 'application/json',
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
                contentType: 'application/json',
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

function receiptsReducer() {
    return function (state, action) {
        switch (action.type) {
            case 'LOAD_RECEIPTS':
                const oldReceipts = state.receipts;
                const newReceipts = [];
                const receipts = Array.isArray(action.payload) ? action.payload : [action.payload];

                receipts.forEach(function (receipt) {
                    var updatedReceipt = false;
                    oldReceipts.forEach(function (oldReceipt, index) {
                        if (!updatedReceipt && oldReceipt.id === receipt.id) {
                            oldReceipts[index] = receipt;
                            updatedReceipt = true;
                        }
                    });

                    if (!updatedReceipt) {
                        newReceipts.push(receipt);
                    }
                });

                state = Object.assign({}, state, {receipts: oldReceipts.concat(newReceipts)});
                break;

            case 'TAG_RECEIPT':
                state.receipts.forEach(function (receipt, index) {
                    if (receipt.id === action.payload.id) {
                        const tagIndex = receipt.tags.indexOf(action.payload.tagName);
                        if (tagIndex !== -1) {
                            receipt.tags.splice(tagIndex, 1);
                        } else {
                            receipt.tags.push(action.payload.tagName);
                        }

                        state.receipts[index] = Object.assign({}, receipt);
                    }
                });

                state = Object.assign({}, state, {receipts: state.receipts});
                break;

            case 'INIT_STATE':
                state = Object.assign({}, state, {receipts: []});
                break;
        }

        console.log(state.receipts.length + ' receipts');

        return state;
    }
}