/*
 *    Copyright (c) 2019, Needham Software LLC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/**
 * Reusable rendering code, for rendering table cells in data tables. Cells with actions usually
 * (but not always) require that an element in the rendered output have the data object for the row
 * attached to an attribute named 'data', so that it can be retrieved by the action code in actions.js
 */

/// --- NOTE: This is hosted in /rest but it can be moved to the app area where other UI lives

const NS_RENDER = (function () {
  /** @namespace data.invitedDate */
  /** @namespace data.roles */
  /** @namespace currentRelation.familyName */
  /** @namespace currentRelation.givenName */
  /** @namespace topic.general */
  /** @namespace topic.specific */
  /** @namespace doc.articleId */
  /** @namespace doc.interactionType */
  /** @namespace data.customer */

  let _this = {};

  function renderTimeStampFromMs(ms) {
    // requires moment.js
    return moment(ms).format('MMM DD, YYYY h:mm a');
  }

  function renderDateFromMs(ms) {
    // requires moment.js
    return moment(ms).format('MMM. DD');
  }

  function renderCheckBoxAndId(id, data) {
    let $input = $("<input type='checkbox'>");
    $input.prop('id', 'item_' + id);
    let input = $input[0];
    input.data = data;
    return input;
  }

  function renderNameOfUser(user) {
    // doesn't actually match AppUser, included as example only
    if (!user) {
      return "Anonymous";
    }
    if (user.familyName || user.givenName) {
      return user.givenName + ' ' + user.familyName
    } else {
      return user.email
    }
  }

  _this.getRenderer = function (name) {
    switch (name) {
      case "dateFromMs":
        return {render: renderDateFromMs};
      case "timestampFromMs":
        return {render: renderTimeStampFromMs};
      case "checkBoxId":
        return {render: renderCheckBoxAndId};
      case "name_of_user" :
        return {render: renderNameOfUser};
    }
  };

  return _this;

})();