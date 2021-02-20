$(document).ready(function() {
    let csrf;
    let stompClient = null;
    let username;
    let state;
    let ready = false;
    let joined = false;
    setCsrf();

    function setCsrf() {
        let request = new XMLHttpRequest();
        request.open("GET", "/csrf");
        request.onload = function() {
            csrf = this.responseText;
            connect();
        };
        request.send();
    }

    function connect() {
        let socket = new SockJS('/secured/websocket');
        console.log(socket);
        stompClient = Stomp.over(socket);
        stompClient.connect({'X-CSRF-TOKEN': csrf}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/secured/user/queue/match/message', function (data) {
                let body = JSON.parse(data.body);
                setResponseBoxMessage(body.message);
                console.log(body);
            });

            stompClient.subscribe('/secured/user/queue/match/command', function (data) {
                let body = JSON.parse(data.body);
                processResponse(body);
                console.log(body);
            });

            stompClient.subscribe('/secured/user/queue/match/map', function (data) {
                let body = JSON.parse(data.body);
                downloadMap(body.content);
                setResponseBoxMessage("Saved the map!");
                console.log(body);
            });

            stompClient.subscribe('/secured/user/queue/match/event', function (data) {
                let body = JSON.parse(data.body);
                processEvent(body);
                console.log(body);
            });

            stompClient.subscribe('/secured/user/queue/match/state', function (data) {
                let body = JSON.parse(data.body);
                processStateChange(body);
                console.log(body);
            });

        });
    }

    $("#submit").click(function () {
        if (!joined) {
            joined = true;
            stompClient.send("/app/secured/match/join", {}, {});
            changeToReadyButton();
        } else if (ready) {
            sendCommand( getInputContent());
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
        stompClient.send("/app/secured/match/ready", {}, {});
    }

    function statusChange(response) {
        let lootedFromRoom = false;
        if(response.command === "forward" || response.command === "backward") {
            let lootJson = JSON.parse(response.data);
            lootedFromRoom = lootJson.gold !== 0 || lootJson.items.length !== 0;
        }

        return state !== "FIGHT" &&
            (response.command.startsWith("buy") || response.command.startsWith("sell") ||
            response.command === "check" || lootedFromRoom ||
            response.command === "left" || response.command === "right");
    }

    function requestPlayerStatus() {
        sendCommand("playerstatus");
    }

    function sendCommand(command) {
        const messageJson = { "command": command }
        stompClient.send("/app/secured/match/command", {}, JSON.stringify(messageJson));
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
            let status = JSON.parse(response.data);
            processPlayerStatus(status);
        } else if(response.command === "save") {
            downloadMap(response.message);
            setResponseBoxMessage("Saved your current progress!");
        } else if(state !== "FIGHT") {
            processCommandResponse(response);
        } else {
            setResponseBoxMessage(response.message);
            console.log(response);
        }

        if(statusChange(response)) {
            requestPlayerStatus();
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
        $.each(items, (i, item) => itemsHtml +=
            "<span class='item item-info'>" + capitalize(formatItem(item)) + "</span>");
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
        setResponseBoxMessage(response.message);
        let data = response.data;
        if(data === undefined || data === "") {
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
            setResponseBoxMessage("A fight has commenced");
        } else {
            joined = false;
            ready = false;
            disableAllInputs();
            resetPlayButton();
        }
    }

    function resetPlayButton() {
        let button = $("#submit");
        button.removeClass("btn-disabled");
        button.prop("disabled", false);
        button.html("Play");
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
        let allButtons = $(".game-container button");
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
            requestPlayerStatus();
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
        sendCommand("left");
    });

    $("#right-btn").click(function() {
        sendCommand("right");
    });

    $("#forward-btn").click(function() {
        sendCommand( "forward");
    });

    $("#backward-btn").click(function() {
        sendCommand( "backward");
    });

    $("#look-btn").click(function() {
        sendCommand( "look");
    });

    $("#check-btn").click(function() {
        sendCommand( "check");
    });

    $("#trade-btn").click(function() {
        sendCommand( "trade");
    });

    $("#list-btn").click(function() {
        sendCommand( "list");
    });

    $("#sell-btn").click(function() {
        sendCommand( "sell " + getInputContent());
    });

    $("#buy-btn").click(function() {
        sendCommand( "buy " + getInputContent());
    });

    $("#finish-trade-btn").click(function() {
        sendCommand( "finish trade");
    });

    $("#use-btn").click(function() {
        sendCommand( "use " + getInputContent());
    });

    $("#lightswitch-btn").click(function() {
        sendCommand( "switchlights");
    });

    $("#save-btn").click(function() {
        sendCommand( "save");
    });

    $("#quit-btn").click(function() {
        sendCommand( "quit");
    })

    $("#download-btn").click(function () {
        stompClient.send("/app/secured/match/map", {}, {});
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