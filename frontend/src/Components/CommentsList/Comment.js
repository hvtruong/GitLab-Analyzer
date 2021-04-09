import React from 'react';
import { OverlayTrigger, Tooltip } from 'react-bootstrap';
import { utcToLocal } from 'Components/Utils/formatDates';

import './Comment.css';

const Comment = (props) => {
    const { creationDate, onOwnRequestOrIssue, url, commentDescription } = props || {};
    return (
        <tr className='comment-row'>
            <td className='is-own'>{onOwnRequestOrIssue ? ("Yes") : ("No")}</td>
            <td className='creation-date'>{utcToLocal(creationDate)}</td>
            <td className='title'>
                <a href={url} target='_blank' rel='noreferrer'>View</a>
            </td>
            <td className='comment-description'>{commentDescription.length < 100 ? commentDescription : 
                (<OverlayTrigger
                    placement="top"
                    overlay={
                        <Tooltip className='tooltip'>
                            {commentDescription}
                        </Tooltip>
                    }
                >
                    <p>{commentDescription.slice(0, 100) + '...'}</p>
                </OverlayTrigger>)}
            </td>
            <td className='word-count'>{commentDescription.match(/(\w+)/g).length}</td>
        </tr>
    );
};

export default Comment;
