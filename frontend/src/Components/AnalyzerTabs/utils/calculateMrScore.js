const calculateCommitScore = (mergerequests) => {
    let sumOfMRs = 0;
    for (let mergeRequest of mergerequests) {
        sumOfMRs += mergeRequest?.mrscore;
        console.log(mergerequests);
    }
    sumOfMRs = Math.round(sumOfMRs * 10) / 10;
    return sumOfMRs;
}

export default calculateCommitScore;