import React from "react";

export default class Button extends React.Component {
  render() {
    return (
      <button
        className={`btn${this.props.small ? "-sm" : ""} btn-${this.props.style || "secondary"}`}
        disabled={this.props.disabled}
        onClick={this.props.onClick}
        title={this.props.title}
        type="button"
      >
        {this.props.children}
      </button>
    )
  }
}
