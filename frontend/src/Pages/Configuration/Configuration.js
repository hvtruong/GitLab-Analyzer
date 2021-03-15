import React, { useState, useEffect } from 'react';
import { Table, Spinner, Button } from 'react-bootstrap';

import Config from 'Components/Configurations/Config';
import ConfigDetails from 'Components/Configurations/ConfigDetails';
import ConfigModal from 'Components/Configurations/ConfigurationModal/ConfigModal'
import getConfigurations from 'Utils/getConfigurations';
import getConfigurationInfo from 'Utils/getConfigurationInfo';
import { useUserState } from 'UserContext';
import './Configuration.css';
import ConfigDefault from 'Components/Configurations/ConfigDefault';
import { defaultConfig } from 'Mocks/mockConfigs.js';

const ConfigurationPage = () => {

    const [selectedConfig, setSelectedConfig] = useState("");
    const [isLoadingConfigs, setIsLoadingConfigs] = useState(true);
    const [isLoadingConfigInfo, setIsLoadingConfigInfo] = useState(true);
    const [configInfo, setConfigInfo] = useState();
    const [configs, setConfigs] = useState([]);
    const username = useUserState();

    const handleClick = (config) => {
        if (config.fileName === "default") {
            setConfigInfo(config)
            setSelectedConfig(config);
            setIsLoadingConfigInfo(false);
        }
        else {
            getConfigurationInfo(username, config).then((data) => {
                setConfigInfo(data);
                setSelectedConfig(config);
                setIsLoadingConfigInfo(false);
            });
        }
    }

    const handleShow = () => setShow(true);
    const handleClose = () => setShow(false);
    const [show, setShow] = useState(false);

    useEffect(() => {
        getConfigurations(username).then((data) => {
            setConfigs(data);
            setIsLoadingConfigs(false);
        });
    }, [username]);

    return (
    <div className = 'configs-list-container'>
        <div className="configs-left">
            <Table striped bordered hover variant="light">
                <thead>
                    <tr>
                        <Button variant="info" onClick={handleShow}>Add New Configuration</Button>
                    </tr>
                        {show && <ConfigModal status={show} toggleModal={handleClose}/>}
                    <tr>
                        <th colSpan='3' className='configTitle'>
                            Configuration Titles
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <ConfigDefault defaultConfig={defaultConfig} handleClick={handleClick}/>
                    {!isLoadingConfigs && configs?.length > 0 && configs.map((config) => (
                        <Config key={config} config={config} handleClick={handleClick}/>
                    ))}
                </tbody>
            </Table>
        </div>
        <div className="configs-right">
            {selectedConfig && isLoadingConfigInfo && <Spinner animation="border" className="right-spinner" />}
            {selectedConfig && !isLoadingConfigInfo && <ConfigDetails configInfo={configInfo} />}
        </div>
    </div>
    )
}

export default ConfigurationPage;