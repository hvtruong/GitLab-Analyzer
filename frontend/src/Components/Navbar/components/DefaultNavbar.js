import React from 'react';
import { Nav, Navbar } from 'react-bootstrap';

import { config } from 'Constants/constants';

const DefaultNavbar = ()=> {

    return (
        <Navbar variant='primary' expand="sm">
            <Navbar.Brand href="/">GitLab Analyzer</Navbar.Brand>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse className="justify-content-end" id="basic-navbar-nav">
                <Nav className="nav">
                    <Nav.Link href={config.SFU_LOGIN_URL}>Sign In</Nav.Link>
                    <Nav.Link href="/About/">About</Nav.Link>
                </Nav>
            </Navbar.Collapse>
        </Navbar>
    )   
}

export default DefaultNavbar