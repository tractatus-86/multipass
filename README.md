LeelooDallasMultipass


Backstory and Motivation:
My music collection is a mess. I have collected few terabytes of music over the years some of which has been duplicated and/or poor quality. I tried using a number of music organization software and I have not been happy with the uncertainty (implicit and explicit) or the results. So I have decided to take a stab at it myself. 

Description:
Music Organzation will work in four stages:

Pass 1: Fire
A songa is md5 hashed when there is a hash collision the colliding song gets moved into the fire duplicates directory.

Pass 2: Wind WIP
Remaining songs are fingerprinted by fpcalc then identified in the AcoustID webservice.
Their tags are then updated with the data retrieved with the AcoustID webservice.
Undentified songs get uniquely tagged and moved into the mystery directory.

Pass 3: Water WIP
Songs with matching fingerprint results get moved into the water duplicates directory.

Pass 4: Earth WIP
The file is then moved into new directories that are generated from the tag info. Ideally Bitrate->Artist->Album->Song.

Secondary Thoughts:
It would be ideal to be able to some how link similar sounding songs so that they can be easily loaded into playlists. Unfortunatily AcoustID does not have genre or similarity integrated into its matching system.