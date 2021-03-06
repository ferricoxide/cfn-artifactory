#!/bin/bash
# shellcheck disable=SC2015,SC2086
#
# Move artifactory backups to S3
#
#################################################################
PROGNAME="$(basename $0)"
S3BUCKET="##ARTIFACTORY_BUCKET##"
S3FOLDER="backups"
BAKUPDIR="/var/backups"

# Error/exit routine
function err_exit {
   local ERRSTR="${1}"

   echo "${ERRSTR}" > /dev/stderr
      logger -t "${PROGNAME}" -p kern.crit "${ERRSTR}"

   exit 1
}


# Identify backup targets
BACKUPFILES=($(find "${BAKUPDIR}" -name lost+found -prune \
               -o -type f -name "*.zip" -print))

# Backup any ZIPfile found in staging directory
if [[ ${#BACKUPFILES[@]} -gt 0 ]]
then
   for BKUP in "${BACKUPFILES[@]}"
   do
      BKUPF="$(basename ${BKUP})"
      echo "Found ${#BACKUPFILES[@]} staged backup files in ${BAKUPDIR}"
      FOUND=$(aws s3 ls "s3://${S3BUCKET}/${S3FOLDER}/${BKUPF}" > /dev/null)$?
      if [[ ${FOUND} -ne 0 ]]
      then
         echo "Backing up ${BKUP} to S3..."
         aws s3 cp "$BKUP" s3://${S3BUCKET}/${S3FOLDER}/ && \
           echo "${BKUPF} copied to s3://${S3BUCKET}/${S3FOLDER}/" || \
	   err_exit 'Received error from S3 copy operation'
         printf "Deleting %s..." "${BKUPF}"
         rm "${BKUP}" && echo success || \
           echo "Failed to clean out ${BKUP}"
      else
         echo "$BKUP already exists in s3://${S3BUCKET}/${S3FOLDER}/. Skipping."
      fi
   done
else
   echo "Found no staged backup files in ${BAKUPDIR}"
fi
