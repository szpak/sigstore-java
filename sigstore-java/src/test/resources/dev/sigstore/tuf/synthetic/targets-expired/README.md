# Setup test data

```shell
cp ../test-template/2.root.json .
cp ../test-template/timestamp.json .
cp -R ../root-signing-workspace ./tmp
cd tmp
# modify the targets to have a version which doesn't match the snapshot.
jq -r '.signed.expires |= "2022-11-19T18:07:27Z"' repository/targets.json | sponge repository/targets.json
# re-sign the targets.json so that it looks trusted
tuf payload targets.json > payload.targets.json  
tuf sign-payload --role=targets payload.targets.json > targets.sigs
tuf add-signatures --signatures targets.sigs targets.json 
cp staged/targets.json ../.
 # update the targets hash in snapshot so it's valid.
jq -r --arg sha "$(sha512sum staged/targets.json | awk '{ print $1 }')" '.signed.meta."targets.json".hashes.sha512 |= $sha' repository/snapshot.json | sponge repository/snapshot.json
# re-sign the snapshot.json now that we've altered it
tuf payload snapshot.json > payload.snapshot.json  
tuf sign-payload --role=snapshot payload.snapshot.json > snapshot.sigs
tuf add-signatures --signatures snapshot.sigs snapshot.json 
cp staged/snapshot.json ../.
# update and resign the timestamp.json with the new snapshot.json hash
jq -r --arg sha "$(sha512sum staged/snapshot.json | awk '{ print $1 }')" '.signed.meta."snapshot.json".hashes.sha512 |= $sha' repository/timestamp.json | sponge repository/timestamp.json
tuf payload timestamp.json > payload.timestamp.json  
tuf sign-payload --role=timestamp payload.timestamp.json > timestamp.sigs
tuf add-signatures --signatures timestamp.sigs timestamp.json 
cp staged/timestamp.json ../.
```
