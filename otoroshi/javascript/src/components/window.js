import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import _ from 'lodash';

class Alert extends Component {
  defaultButton = e => {
    if (e.keyCode === 13) {
      this.props.close();
    }
  }
  componentDidMount() {
    document.body.addEventListener('keydown', this.defaultButton);
  }
  componentWillUnmount() {
    document.body.removeEventListener('keydown', this.defaultButton);
  }
  render() {
    const res = _.isFunction(this.props.message) ? this.props.message(this.props.close) : this.props.message;
    return (
      <div className="modal" tabindex="-1" role="dialog" style={{ display: 'block' }}>
        <div className="modal-dialog" role="document">
          <div className="modal-content">
            <div className="modal-header">
              <button type="button" className="close" data-dismiss="modal" onClick={this.props.close} aria-label="Close">
                <span aria-hidden="true">&times;</span>
              </button>
              <h4 className="modal-title">{this.props.title ? this.props.title : 'Alert'}</h4>
            </div>
            <div className="modal-body">
              {_.isString(res) && <p>{res}</p>}
              {!_.isString(res) && !_.isFunction(res) && res}
              {!_.isString(res) && _.isFunction(res) && res(this.props.close)}
            </div>
            <div className="modal-footer">
              {this.props.linkOpt && (
                  <a
                    data-dismiss="modal"
                    href={this.props.linkOpt.to}
                    className="btn btn-default"
                    onClick={this.props.close}>
                    {this.props.linkOpt.title}
                  </a>
                )}
                <button type="button" className="btn btn-primary" onClick={this.props.close}>
                  Close
                </button>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

class Confirm extends Component {
  defaultButton = e => {
    if (e.keyCode === 13) {
      this.props.ok();
    }
  }
  componentDidMount() {
    document.body.addEventListener('keydown', this.defaultButton);
  }
  componentWillUnmount() {
    document.body.removeEventListener('keydown', this.defaultButton);
  }
  render() {
    return (
      <div className="modal" tabindex="-1" role="dialog" style={{ display: 'block' }}>
        <div className="modal-dialog" role="document">
          <div className="modal-content">
            <div className="modal-header">
              <button type="button" className="close" data-dismiss="modal" onClick={this.props.cancel} aria-label="Close">
                <span aria-hidden="true">&times;</span>
              </button>
              <h4 className="modal-title">{this.props.title ? this.props.title : 'Confirm'}</h4>
            </div>
            <div className="modal-body">
              <p>{this.props.message}</p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-danger" onClick={this.props.cancel}>
                Cancel
              </button>
              <button type="button" className="btn btn-success" onClick={this.props.ok}>
                Ok
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

class Prompt extends Component {
  state = {
    text: this.props.value || '',
  };
  defaultButton = e => {
    if (e.keyCode === 13) {
      this.props.ok(this.state.text);
    }
  }
  componentDidMount() {
    document.body.addEventListener('keydown', this.defaultButton);
    if (this.ref) {
      this.ref.focus();
    }
  }
  componentWillUnmount() {
    document.body.removeEventListener('keydown', this.defaultButton);
  }
  render() {
    return (
      <div className="modal" tabindex="-1" role="dialog" style={{ display: 'block' }}>
        <div className="modal-dialog" role="document">
          <div className="modal-content">
            <div className="modal-header">
              <button type="button" className="close" data-dismiss="modal" onClick={this.props.cancel} aria-label="Close">
                <span aria-hidden="true">&times;</span>
              </button>
              <h4 className="modal-title">{this.props.title ? this.props.title : 'Prompt'}</h4>
            </div>
            <div className="modal-body">
              <p>{this.props.message}</p>
              <input
                type="text"
                className="form-control"
                value={this.state.text}
                ref={r => this.ref = r}
                onChange={e => this.setState({ text: e.target.value })}
              />
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={this.props.cancel}>
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={e => this.props.ok(this.state.text)}>
                Ok
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export function registerAlert() {
  window.oldAlert = window.alert;
  if (!document.getElementById('otoroshi-alerts-container')) {
    const div = document.createElement('div');
    div.setAttribute('id', 'otoroshi-alerts-container');
    document.body.appendChild(div);
  }
  window.newAlert = (message, title, linkOpt) => {
    return new Promise(success => {
      ReactDOM.render(
        <Alert
          message={message}
          title={title}
          linkOpt={linkOpt}
          close={() => {
            ReactDOM.unmountComponentAtNode(document.getElementById('otoroshi-alerts-container'));
            success();
          }}
        />,
        document.getElementById('otoroshi-alerts-container')
      );
    });
  };
}

export function registerConfirm() {
  window.oldConfirm = window.confirm;
  if (!document.getElementById('otoroshi-alerts-container')) {
    const div = document.createElement('div');
    div.setAttribute('id', 'otoroshi-alerts-container');
    document.body.appendChild(div);
  }
  window.newConfirm = message => {
    return new Promise((success, failure) => {
      ReactDOM.render(
        <Confirm
          message={message}
          ok={() => {
            success(true);
            ReactDOM.unmountComponentAtNode(document.getElementById('otoroshi-alerts-container'));
          }}
          cancel={() => {
            success(false);
            ReactDOM.unmountComponentAtNode(document.getElementById('otoroshi-alerts-container'));
          }}
        />,
        document.getElementById('otoroshi-alerts-container')
      );
    });
  };
}

export function registerPrompt() {
  window.oldPrompt = window.prompt;
  if (!document.getElementById('otoroshi-alerts-container')) {
    const div = document.createElement('div');
    div.setAttribute('id', 'otoroshi-alerts-container');
    document.body.appendChild(div);
  }
  window.newPrompt = (message, value) => {
    return new Promise((success, failure) => {
      ReactDOM.render(
        <Prompt
          message={message}
          value={value}
          ok={inputValue => {
            success(inputValue);
            ReactDOM.unmountComponentAtNode(document.getElementById('otoroshi-alerts-container'));
          }}
          cancel={() => {
            success(null);
            ReactDOM.unmountComponentAtNode(document.getElementById('otoroshi-alerts-container'));
          }}
        />,
        document.getElementById('otoroshi-alerts-container')
      );
    });
  };
}