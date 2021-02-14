$(document).ready(function() {
    let ws = null;
    let ready = false;
    let username;
    let state;

    $("#submit").click(function () {
        if (ws === null) {
            createWebSocket();
            changeToReadyButton();
        } else if (ready) {
            sendMessage("command", getInputContent());
        } else {
            makeReady();
        }
    });

    function changeToReadyButton() {
        let submitButton = $("#submit");
        submitButton.removeClass("btn-disabled");
        submitButton.html("Ready");
    }

    function makeReady() {
        ready = true;
        $("#submit").html("Send");
        disableAllInputs();
        sendMessage("ready", "");
    }

    function createWebSocket() {
        ws = new WebSocket("ws://localhost:4567/websocket/match");

        ws.onopen = function (event) {
            console.log(event);
            username = getInputContent();
            sendMessage("username", username);
            $("#inputText").prop("disabled", true);
        }

        ws.onclose = function (event) {
            console.log(event);
            setResponseBoxData("<span>" + event.reason + "</span>");
        }

        ws.onmessage = function (event) {
            console.log(event);
            let response = JSON.parse(event.data);
            if(response.type === "response") {
                processResponse(response);
            } else if(response.type === "stateChange") {
                processStateChange(response);
            } else if (response.type === "map") {
                downloadMap(response.content);
                setResponseBoxMessage("Saved the map!");
            } else if(response.type === "event") {
                processEvent(response);
            } else {
                console.log(response);
            }

            if(statusChange(response)) {
                sendMessage("command", "playerstatus");
            }
        }
    }

    function statusChange(response) {
        return response.type === "response" && (response.command.startsWith("buy") ||
            response.command.startsWith("sell") || response.command === "check" ||
            response.command === "forward" || response.command === "backward" ||
            response.command === "left" || response.command === "right") ||
            (response.type === "event" && response.eventType === "SENDING_PLAYER_LIST");
    }

    function sendMessage(type, content) {
        const messageJson = {
            "type": type,
            "content": content
        }
        ws.send(JSON.stringify(messageJson));
    }

    function setResponseBoxMessage(text) {
        $(".response-container>.message").html(text);
    }

    function setResponseBoxData(html) {
        $(".response-container>.data").html(html);
    }

    // processing response to requests type messages
    function processResponse(response) {
        if(response.command === "playerstatus") {
            let status = JSON.parse(response.content.data);
            processPlayerStatus(status);
        } else if(response.command === "save") {
            downloadMap(response.content.message);
            setResponseBoxMessage("Saved your current progress!");
        } else if(state !== "FIGHT") {
            processCommandResponse(response);
        } else {
            console.log(response);
        }
    }

    function processPlayerStatus(status) {
        $(".orientation").html(capitalize(status.orientation));
        $(".gold").html(status.gold);
        let itemsHtml = getFormattedItems(status.items);
        $(".items").html(itemsHtml);
    }

    function getFormattedItems(items) {
        let itemsHtml = "";
        $.each(items, (i, item) => itemsHtml += "<span class='item item-info'>" + formatItem(item) + "</span>" );
        return itemsHtml;
    }

    function formatItem(item) {
        return item.name + (item.name === "" ? "" : " ") + item.type;
    }

    function downloadMap(mapJson) {
        downloadObjectAsJson(JSON.parse(mapJson), username);
        setResponseBoxData("");
    }

    function downloadObjectAsJson(data, fileName) {
        let fileToSave = new Blob([JSON.stringify(data)], {
            type: 'application/json',
            name: fileName
        });

        saveAs(fileToSave, fileName);
    }

    function processCommandResponse(response) {
        setResponseBoxMessage(response.content.message);
        let data = response.content.data;
        if(data === undefined) {
            setResponseBoxData("");
        } else {
            data = JSON.parse(data);
            if(canAcquireLoot(response.command)) {
                setResponseBoxData(formatAcquiredLoot(data));
            } else if(listingTradeItems(response.command)) {
                let listingHtml = "<div><span>Available items:</span>" +
                    getFormattedItems(data.items) + "</div>" + getFormattedPriceList(data.priceList);
                setResponseBoxData(listingHtml);
            }
        }
    }

    function canAcquireLoot(command) {
        return command === "check" || command === "forward" || command === "backward";
    }

    function listingTradeItems(command) {
        return command === "trade" || command === "list";
    }

    function getFormattedPriceList(priceListJson) {
        let priceListHTML = "<div><span>Price list: </span>";
        $.each(priceListJson, (i, item) => priceListHTML += "<span class='item-info'>" + formatListing(item) + "</span>" );
        return priceListHTML + "</div>";
    }

    function formatListing(listing) {
        let name = capitalize(listing.name);
        return "<span class='item'>" + name + "</span>" + " for $" + listing.price;
    }

    function capitalize(string) {
        return string.toLowerCase().split(" ").map(word => word.charAt(0).toUpperCase() + word.substring(1)).join(' ');
    }

    function formatAcquiredLoot(lootJson) {
        if(lootJson.gold === 0 && lootJson.items.length === 0) {
            return "<span>Found nothing!</span>";
        }

        let messageHtml = "<div class='loot'><h3>Acquired the following:</h3>";
        messageHtml += "<div><span class='item-info loot-gold'>" + lootJson.gold + " Gold</span>";
        messageHtml += getFormattedItems(lootJson.items) + "</div></div>";
        return messageHtml;
    }

    // processing state type messages
    function processStateChange(message) {
        changeState(message.state);
        if(message.content !== "") {
            setResponseBoxMessage(message.content);
        }
        setResponseBoxData("");
    }

    function changeState(state) {
        if(state === "EXPLORE") {
            setExploreState();
        } else if(state === "TRADE") {
            setTradeState();
        } else if(state === "FIGHT") {
            setFightState();
        } else {
            disableAllInputs();
        }
    }

    function setExploreState() {
        state = "EXPLORE";
        disableAllInputsExceptMisc();
        let navigationButtons = $(".navigation button");
        navigationButtons.removeClass("btn-disabled");
        navigationButtons.prop("disabled", false);
    }

    function setTradeState() {
        state = "TRADE";
        disableAllInputsExceptMisc();
        let tradeButtons = $(".trade button");
        tradeButtons.removeClass("btn-disabled");
        tradeButtons.prop("disabled", false);
    }

    function setFightState() {
        state = "FIGHT";
        disableAllInputsExceptMisc();
        let sendButton = $("#submit");
        sendButton.removeClass("btn-disabled");
        sendButton.prop("disabled", false);
    }

    function disableAllInputsExceptMisc() {
        disableAllInputs();
        enableMiscButtons();
    }

    function enableMiscButtons() {
        let miscButtons = $(".misc button");
        miscButtons.removeClass("btn-disabled");
        miscButtons.prop("disabled", false);
    }

    function disableAllInputs() {
        let allButtons = $("button");
        allButtons.addClass("btn-disabled");
        allButtons.prop("disabled", true);
        let textInput = $("#inputText");
        textInput.val("");
        textInput.prop("placeholder", "");
        textInput.prop("disabled", true);
    }

    // processing event type messages
    function processEvent(message) {
        setResponseBoxData("");
        if(message.eventType === "START_MATCH") {
            setResponseBoxMessage(message.content);
            setExploreState();
        } else if(message.eventType === "SENDING_PLAYER_LIST") {
            let playersHtml = getPlayerListHtml(message.content);
            $(".player-names").html(playersHtml);
        } else if(message.eventType === "TIE_FIGHT") {
            setResponseBoxMessage(message.content);
        } else if(message.eventType === "REQUESTING_INPUT") {
            setResponseBoxData("<br/><span> Please enter an answer: " + message.content + "</span><br/>");
            enableSending();
        } else {
            console.log("Unknown eventType " + message.eventType);
        }
    }

    function getPlayerListHtml(listAsString) {
        let playerList = listAsString.slice(1, -1).split(",").map(name => name.trim());
        let playersHtml = "";
        $.each(playerList, (i, player) => playersHtml += "<span class='player-name'>" + player + "</span>");
        return playersHtml;
    }

    function enableSending() {
        $("#inputText").prop("disabled", false);
        $("#submit").prop("disabled", false);
    }

    // Event binding
    $("#left-btn").click(function() {
        sendMessage("command", "left");
    });

    $("#right-btn").click(function() {
        sendMessage("command", "right");
    });

    $("#forward-btn").click(function() {
        sendMessage("command", "forward");
    });

    $("#backward-btn").click(function() {
        sendMessage("command", "backward");
    });

    $("#look-btn").click(function() {
        sendMessage("command", "look");
    });

    $("#check-btn").click(function() {
        sendMessage("command", "check");
    });

    $("#trade-btn").click(function() {
        sendMessage("command", "trade");
    });

    $("#list-btn").click(function() {
        sendMessage("command", "list");
    });

    $("#sell-btn").click(function() {
        sendMessage("command", "sell " + getInputContent());
    });

    $("#buy-btn").click(function() {
        sendMessage("command", "buy " + getInputContent());
    });

    $("#finish-trade-btn").click(function() {
        sendMessage("command", "finish trade");
    });

    $("#use-btn").click(function() {
        sendMessage("command", "use " + getInputContent());
    });

    $("#lightswitch-btn").click(function() {
        sendMessage("command", "switchlights");
    });

    $("#save-btn").click(function() {
        sendMessage("command", "save");
    });

    $("#quit-btn").click(function() {
        sendMessage("command", "quit");
    })

    $("#download-btn").click(function () {
        sendMessage("map", "");
    });

    function getInputContent() {
        return $("#inputText").val();
    }

    $(document).on("click",'.item-info',function() {
        let element = $(this);
        if(element.hasClass("item")) {
            $("#inputText").val(this.innerText);
        } else {
            let child = element.children(".item")[0];
            if(child !== undefined) {
                $("#inputText").val(child.innerText);
            }
        }
    });

});