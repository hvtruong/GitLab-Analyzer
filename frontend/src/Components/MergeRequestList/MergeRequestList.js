import React from 'react';
import { Table } from 'react-bootstrap';

import { message } from 'Constants/constants';

import './MergeRequestList.css';
import MergeRequest from './MergeRequest';
import { useSortableDataObject, getClassNamesFor } from 'Utils/sortTables';


const MergeRequestList = (props) => {
    const { mergerequests, setCommit, setCodeDiffs } = props || {};
    const { items, requestSortObject, sortConfig  } = useSortableDataObject(mergerequests);
    const handleClick = (commits, diffs) => {
        if(setCodeDiffs) {
            setCodeDiffs(diffs);
        }
        if(setCommit) {
            setCommit(commits);
        }
    }
    return (
        <div className='merge-request-list-container'>
                <Table striped bordered hover variant='light'>
                    <thead>
                        <tr>
                            <th colSpan='7' className='mrTitle'>Merge Requests</th>
                        </tr>
                    </thead>
                    <thead>
                        <tr className='mr-headers'>
                            <th className={getClassNamesFor(sortConfig, 'mergedDate')} onClick={() => requestSortObject('mergedDate')}>Date Merged</th>
                            <th className={getClassNamesFor(sortConfig, 'mergeRequestTitle')} onClick={() => requestSortObject('mergeRequestTitle')}>Title</th>
                            <th className={getClassNamesFor(sortConfig, 'mrscore')} onClick={() => requestSortObject('mrscore')}>MR Score</th>
                            <th className={getClassNamesFor(sortConfig, 'sumOfCommitScore')} onClick={() => requestSortObject('sumOfCommitScore')}>Commits Score</th>
                            <th className={getClassNamesFor(sortConfig, 'commitDTOList')} onClick={() => requestSortObject('commitDTOList')}># Commits</th>
                            <th className={getClassNamesFor(sortConfig, 'linesAdded')} onClick={() => requestSortObject('linesAdded')}>Lines +</th>
                            <th className={getClassNamesFor(sortConfig, 'linesRemoved')} onClick={() => requestSortObject('linesRemoved')}>Lines -</th>
                        </tr>
                    </thead>
                    {console.log(mergerequests)}
                    <tbody>
                        {!mergerequests?.length ? (
                            <td colSpan='8' >{message.NO_MERGE_REQUEST}</td>
                        )
                        :
                        items.map((mergerequest) => (
                            <MergeRequest key={mergerequest?.mergeId} mergerequest={mergerequest} handleClick={handleClick} />
                        ))}

                    </tbody>
                </Table>
        </div>
    )
}


export default MergeRequestList
