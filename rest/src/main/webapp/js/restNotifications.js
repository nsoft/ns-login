(function (window) {
  /** @namespace notification.notificationType */

    // add a copy of this file (renamed to avoid confusion) to any context that should supply messages.
    // Edit the REST_CTX below appropriately, and reference this file from the page that should
    // receive messages. You will also need to install the WebsocketFilter and properly
    // initialize the Messages class (see GuiceContextListener for an example)

  let REST_CTX = CONTEXT + 'rest/';

  function newSocket(delay) {
    let timeout;
    let socket;
    if (!window.SC_NOTIFICATION_SOCKET || window.SC_NOTIFICATION_SOCKET.readyState !== 1) {
      timeout = window.setTimeout(function () {
        newSocket(delay * 1.5)
      }, delay); // exponential backoff
      let uri = new URI();
      let scheme = uri.scheme() === 'https' ? 'wss://' : 'ws://';  // note ws only to be used on dev desktops!
      let base = scheme + uri.host();
      socket = new WebSocket(base + REST_CTX + "socket/notifications");
      window.clearTimeout(timeout);
      window.REST_SOCKET = socket;
      socket.onmessage = function (msgEvt) {
        let $messages = $('.messages');
        let $templateMsg = $messages.find('.hidden-template');
        let $ul = $messages.find('ul');
        let $items = $ul.find('li');
        let notification = JSOG.parse(msgEvt.data);
        let type = notification.notificationType;
        let message = notification.message;
        let fade = notification.displayMs;
        let link = notification.link;
        let onClickFn = notification.onClickFn;
        switch (type) {
          case 'ERROR' : {
            REST.displayErrorMessage(message, false);
            break;
          }
          case 'WARNING' :
          case 'SUCCESS' :
          case 'INFO' : {
            for (let i = 3; i < $items.length; i++) {
              let $item = $($items[i]);
              $item.fadeOut(300, (function (toRemove) {
                return function () {
                  toRemove.remove()
                }
              })($item));
            }
            REST.displayMessage($templateMsg, message, $ul, type, link, onClickFn, fade);
            break;
          }
        }
      };
      socket.onopen = function () {
        $.get(REST_CTX + "messages/")
      };
    }
    return socket;
  }

  jQuery(document).ready(function () {
    newSocket(100);
  });

})(window);
