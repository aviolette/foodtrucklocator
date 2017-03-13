let React = require('react'),
    FormGroup = require('react-bootstrap').FormGroup,
    ControlLabel = require('react-bootstrap').ControlLabel,
    InputGroup = require('react-bootstrap').InputGroup,
    FormControl = require('react-bootstrap').FormControl,
    Button = require('react-bootstrap').Button;

module.exports = React.createClass({
  render: function () {
    return (
        <div className="row">
          <div className="col-md-12">
            <form method="GET" action="">
              <FormGroup controlId="q">
                <ControlLabel>Location</ControlLabel>
                <InputGroup>
                  <FormControl name="q" id="q" type="text" placeholder="Location name"/>
                  <InputGroup.Button><Button type="submit">Search</Button></InputGroup.Button>
                </InputGroup>
              </FormGroup>
            </form>
          </div>
        </div>
    );
  }
});
